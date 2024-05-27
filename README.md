# <a href="https://ocw.cs.pub.ro/courses/pm/prj2024/rvirtan/wireless-lock" style="color:inherit; text-decoration:none">Închidere centralizată wireless pentru autoturisme</a>

## Introducere

În prezent, majoritatea autoturismelor noi vin cu dotări precum keyless entry și posibilitatea de a-ți conecta mașina la smartphone-ul personal prin intermediul unei aplicații dedicate.

Proiectul presupune implementarea unui sistem de keyless entry controlat din telefon, cât și montarea acestuia pe o mașină (mașina personală) care nu vine dotată cu sistemele menționate mai sus. Aplicația de pe smartphone va permite încuierea mașinii, cât și verificarea stării închiderii centralizate (încuiat/descuiat).

De asemenea, în traficul bucureștean și nu numai, ești lovit de situația în care, după ce ai ajuns la destinație, găsești un loc de parcare care nu-ți aparține, așa că îți lași numărul de telefon "în parbriz" astfel încât să poți fi contactat de proprietar în caz că ajunge acasă și nu își găsește locul liber.

*Dar ce faci în cazul în care nu ai o hârtie sau pix ca să-ți treci numărul de telefon?*

Ei bine, pe lângă sistemul de keyless entry, îți vei putea seta din aplicație datele de contact (prenume și număr de telefon), astfel încât acestea vor fi afișate pe un display.

## Descriere generală

### Schemă bloc

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/schema_bloc.png" class="align-center" width="700" />

### Mod de funcționare

Utilizatorul poate controla din aplicație:

-   închiderea centralizată;
-   ce este afișat pe display;
-   numele modulului HC-05, vizibil în lista de dispozitive Bluetooth asociate pe smartphone.

Aplicația comunică cu placa de dezvoltare Arduino prin intermediul modulului HC-05, trimițând comenzile sus menționate.

## Hardware Design

În realizarea proiectului voi folosi:

-   Arduino UNO R3;
-   Modul Bluetooth HC-05 SPI;
-   LCD 1602 cu backlight albastru I2C;
-   1x LED Roșu;
-   1x Buzzer pasiv;
-   1x Rezistor 150Ω;
-   2x Rezistor 1kΩ;
-   1x Rezistor 2kΩ;
-   1x Diodă 1N4007;
-   1x Tranzistor NPN 2N2222A;
-   Modul alimentare breadboard HW-131;
-   Modul închidere centralizată KEETEC TS-10;
-   Modul releu 1 canal, Low Level Triggered;
-   Škoda Octavia 2, 1.9 TDI, an de fabricație 2007 (cazanul pe care voi monta sistemul).

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/hardware_design.png" class="align-center" width="600" />

Pentru buna funcționare a circuitului de mai sus, designul este alcătuit din:

-   circuitul modul HC-05, LCD și LED martor de armare, conectate la VCC-ul Arduino-ului (partea superioară a breadboard-ului);
-   circuitul buzzer, închidere centralizată, conectate la VCC-ul modulului de alimentare HW-131 (partea inferioară a breadboard-ului).

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/circuit_inchidere_centralizata.png" class="align-center" width="600" />

Am împărțit astfel, deoarece în circuitul buzzer-ului am folosit tranzistorul NPN 2N2222A pentru a amplifica curentul. Buzzerul pasiv este cunoscut pentru faptul că se aude încet, deci prin amplificarea curentului am reușit să măresc volumul buzzer-ului. Problema care apare aici este că amplificarea vine la pachet cu un consum mare de energie atât timp cât este pornit buzzer-ul (am fost lovit de situația în care LCD-ul "pâlpâia" pentru că nu avea suficient curent).

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/setup_hw_131.jpg" class="align-center" width="600" />

După cum se poate observa în setup, partea superioară a sursei de alimentare este setată pe OFF (nu avem nevoie de curent acolo, deoarece am conectat deja VCC-ul și GND-ul de la Arduino), iar partea inferioară am setat-o pe 5V.

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/circuit.jpg" class="align-center" width="600" />

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/lcd_date_contact.jpg" class="align-center" width="600" />

## Software Design

### Arduino UNO R3

Software-ul pentru Arduino UNO R3 a fost realizat în Visual Studio Code, folosind extensia PlatformIO, unde am inclus bibliotecile LCD_I2C.h (îmi facilitează comunicarea cu display-ul) și SoftwareSerial.h (îmi permite să comunic serial cu modulul HC-05 folosind ceilalți pini digitali de pe Arduino).

În implementarea mea am definit două funcții auxiliare: `commandParser()` și `serialFlush()`. Modulul de Bluetooth HC-05 primește de la aplicația de pe telefon o comandă, îndeplinindu-se
condiția `BTSerial.available()`, astfel se apelează funcția de parsare a comenzii, urmând să se execute rutina specifică. Comenzile implementate sunt:

-   `sync` -- trimite înapoi telefonului starea curentă a închiderii centralizate (încuiat/descuiat);
-   `lock` -- încuie/descuie mașina în funcție de starea curentă a închiderii centralizate;
-   `hideInfo:<1>` -- primește un parametru: 0 sau 1. Dacă valoarea parametrului este egală cu 0, atunci display-ul este aprins, afișând datele de contact ale șoferului, altfel display-ul va fi stins, ascunzând datele de contact;
-   `setInfo:<1>,<2>` -- primește doi parametri: numele șoferului și numărul acestuia de telefon (datele de contact pe care vrem să le afișăm pe display). Această comandă poate primi `*` în locul oricăruia dintre parametri, de exemplu: `setInfo:Ciprian,*`. În acest caz, se va actualiza în memoria Arduino-ului doar numele șoferului (valoarea `*` păstrează valoarea curentă din memorie);
-   `setBtCred:<1>,<2>` -- primește doi parametri: numele modulului și PIN-ul acestuia (credențialele pe care vrem să le setăm). La fel ca în cazul comenzii `setInfo`, valoarea `*` păstrează valoarea curentă care este stocată în memorie.

Comanda `setBtCred` trece modulul HC-05 în modul AT (un fel de "sudo"), unde, în funcție de parametrii primiți, construiește comenzile AT specifice operațiunii dorite (`AT+NAME=<1>` setează numele modulului Bluetooth care o să apară în lista de pairing și `AT+PSWD=<2>` setează PIN-ul pe care utilizatorul va trebui să-l introducă în momentul în care realizează pairing-ul cu modulul). La finalul execuției fiecărei comenzi AT, modulul va returna pe serial stringul `OK`, așa că folosesc funcția `serialFlush()` pentru a goli buffer-ul.

Fiecare comandă executată va întoarce pe serial `success` sau `fail`, fiind mai ușoară gestionarea erorilor în aplicația de pe telefon.

În final, în funcția `loop()` am realizat blink-ul pentru martorul de încuiere/descuiere, folosind `millis()` în loc de `delay()`, deoarece nu întrerup execuția instrucțiunilor.

### Aplicația Android

Această aplicație a fost realizată în Android Studio, folosind limbajul de programare Kotlin. Template-ul ales pentru proiect este "Bottom Navigation Views Activity", după care am setat SDK-ul pe nivelul API 31 (Android 12 Snow Cone), adică versiunea minimă de Android care suportă aplicația.

Partea de UI a aplicației este compusă dintr-un MainActivity, care conține un indicator de progres (este ascuns utilizatorului -- afișat la nevoie, de exemplu când se realizează un task), bara de navigație care conține trei butoane spre fiecare meniu și host fragment-ul (un fel de "iframe" din HTML). Toate iconițele pentru butoane și TextInput-uri sunt importate din Vector Asset.

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/fragments.jpg" class="align-center" width="800" />

Toate componentele folosite în aplicație folosesc `Material 3`, ultima versiune de design system creată de Google.

În `HomeFragment` se regăsesc două `MaterialCardView`-uri (pe care le-am făcut clickable, deci sunt butoane) și un `FloatingActionButton`:

-   butonul `Connect` -- la apăsarea lui ești trimis în meniul de selectare a dispozitivului cu care vrei să te conectezi, meniu în care se regăsește un `ListView` într-un `MaterialCardView` și un `FloatingActionButton` (butonul de refresh al listei);

La prima rulare a aplicației de după instalare, se va solicita acordarea permisiunii `Nearby devices`. După acordarea permisiunilor, utilizatorul poate continua spre selectarea dispozitivului dorit. Dacă se refuză prima dată acordarea permisiunii, utilizatorului i se va afișa un `MaterialAlertDialog` (numit și "permission rationale") în care este explicat faptul că aplicația are nevoie de permisiunea respectivă pentru funcționarea corectă. Dacă după acest mesaj se mai refuză o dată acordarea permisiunii, utilizatorul este informat că aplicația va rula cu funcționalități limitate. Dacă acesta se răzgândește, este redirecționat spre pagina de setări a aplicației, de unde poate acorda manual permisiunea necesară.

Toată logica pentru prompt-urile de runtime permissions se află în `HomeFragment.kt`.

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/permission.jpg" class="align-center" width="800" />

-   butonul `Synchronize` -- sincronizează starea închiderii centralizate din aplicație cu starea curentă din Arduino;
-   butonul `Lock/Unlock` -- inițial aplicația nu cunoaște starea închiderii centralizate, așa că este afișat un semn de întrebare (în această stare, butonul funcționează la fel ca butonul de `Synchronize`), însă după realizarea sincronizării cu Arduino-ul va afișa în timp real starea curentă (încuiat/descuiat), iar apăsarea acestuia va încuia/descuia ușile mașinii.

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/lock_button_variations.jpg" class="align-center" width="600" />

În `DisplayFragment` am folosit un `MaterialCardView` în care am adăugat două `TextInput`-uri (unde utilizatorul își va completa datele de contact), un `SwitchMaterial` (poți alege dacă vrei să afișezi datele de contact sau nu) și un `MaterialButton` căruia i-am setat un onClickListener (după apăsarea butonului, aplicația generează comenzile specifice fiecărui câmp completat și le adaugă în coada de execuție a task-urilor).

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/fragment-display-example.jpg" class="align-center" width="800" />

1.  În prima imagine comenzile generate vor fi `setInfo:Ciprian,1234567890123456` și `hideInfo:0`
2.  În a doua imagine comenzile generate vor fi `setInfo:Ciprian,*` și `hideInfo:0`
3.  În a treia imagine comenzile generate vor fi `setInfo:*,1234567890123456` și `hideInfo:0`
4.  În ultima imagine comenzile generate vor fi `setInfo:*,*` și `hideInfo:1`

Pentru punctul 4 comanda `setInfo:*,*` este generată onClick, însă pentru că nu face nimic, aceasta nu va fi adăugată în coada de execuție.

SettingsFragment are un layout și un comportament similar: în loc să genereze comenzile `setInfo` și `hideInfo`, va genera comanda `setBtCred` în funcție de câmpurile completate. Însă, după ce se înregistrează un onClickEvent, utilizatorul este informat că dispozitivul va reporni, iar reconectarea va fi necesară.

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/fragment_settings_dialog.jpg" class="align-center" width="300" />

Pentru a facilita accesul la thread-ul de conexiune, cât și comunicarea cu `MainActivity`, am creat o interfață cu două metode abstracte: `connectTo()` și `getBtConnection()`. Metoda `connectTo()` este apelată din `SelectBtDeviceFragment.kt` pentru a putea trimite mai departe spre prelucrare către MainActivity dispozitivul selectat de utilizator, deoarece ciclul de viață al unui fragment se limitează la perioada în care conținutul acestuia este afișat pe ecran (după ce acesta este scos de pe back stack, ajunge în starea `DESTROYED`, iar toate datele din acel fragment vor fi pierdute).

Din punct de vedere al modului de funcționare, aplicația funcționează pe 2 thread-uri: primul thread (main) se ocupă de UI/UX, iar al doilea thread se ocupă atât de stabilirea conexiunii Bluetooth cu modulul HC-05, cât și de executarea task-urilor din coada de execuție. Pe măsură ce utilizatorul blochează/deblochează mașina, setează datele de contact, etc., thread-ul main (dispatcher-ul) va genera comenzile implementate în Arduino, pe care le va adăuga în coada de execuție a thread-ului de conexiune (`ConnectionThread` cum l-am denumit în implementare).

``` kotlin
val btConnection = activityPipe!!.getBtConnection()
if (contactCommand != "setInfo:*,*") {
    btConnection!!.enqueueJob(this@DisplayFragment, contactCommand)
}
btConnection!!.enqueueJob(this@DisplayFragment, displayCommand)
```

Am ales să folosesc multithreading, deoarece execuția comenzilor din coadă necesită trimiterea acestora pe serial, așteptarea unui răspuns al Arduino-ului (`success` sau `fail`, plus starea curentă a închiderii centralizate -- în cazul comenzii `sync`), ceea ce presupune folosirea unui while (în care am setat un timp maxim de așteptare de 5 secunde, după care inițiez un timeout, care va opri imediat thread-ul și va închide socket-ul de Bluetooth). Această așteptare după un răspuns blochează complet aplicația, deoarece singurul thread disponibil care se ocupa de animații, onClickEvent-uri, etc. este ocupat să execute bucla, fiind un comportament nedorit și de evitat.

## Montare

Pentru montarea efectivă a sistemului wireless pe autoturism, am parcurs următoarele etape:

-   Pentru acces la modulul de închidere centralizată a mașinii, am demontat blocul de lumini, ornamentele care maschează șuruburile și plasticul de sub volan;

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/montare1.jpg" class="align-center" width="500" />

-   Am identificat butonul de încuiere/descuiere al modulului KEETEC TS-10, pe care l-am deconectat;

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/montare2_3.jpg" class="align-center" width="650" />

-   După ce am observat că scurtcircuitarea pinilor 2 și 3 de pe mufa CN3 duce la încuierea/descuierea mașinii, am conectat firul COM la pinul 2 și firul NO la pinul 3.

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/montare4.jpg" class="align-center" width="500" />

## Rezultate Obţinute

În final, circuitul pentru acest proiect și partea software funcționează exact cum am vrut: absolut toată interacțiunea cu sistemul wireless de închidere centralizată se realizează prin intermediul aplicației Android, nemaifiind necesară intervenția utilizatorului asupra componentelor după instalare, de exemplu dacă se dorește schimbarea credențialelor modulului Bluetooth, software-ul se va ocupa atât de modificarea setărilor (includem schimbarea în modul AT fără apăsarea butonului fizic de către utilizator), cât și de repornirea modulului pentru ca acestea să fie aplicate imediat.

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/rezultat.jpg" class="align-right" width="384" />

<iframe width="288" height="512" src="https://www.youtube.com/embed/fjLBnKy166g?si=VYyjpPgA3-yAeKNr" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

## Concluzii

Proiectul a fost unul foarte interesant, dar și challenging. Am avut ocazia să învăț multe lucruri de la zero, în special partea de Android a software-ului, cu care nu mai lucrasem până acum. Mi-a plăcut să lucrez la acest proiect și am învățat multe aspecte noi care îmi vor fi utile pe viitor. Am dobândit cunoștințe valoroase în dezvoltarea de aplicații Android și cred cu tărie că această experiență m-a ajutat să îmi dezvolt abilitățile și să capăt mai multă încredere în lucrul cu tehnologii noi.

De asemenea, mi-a plăcut să îmi "creez" un upgrade (sau dotare) pentru mașina personală și să stau să meșteresc pe sub volan (însă a rămas o adevărată harababură, deci va trebui să găsesc o zi liberă ca să montez la loc ornamentele și plasticele de sub volan LOL).

## Bibliografie/Resurse

### Hardware

[Plusivo Wireless Super Starter Kit with ESP8266
Guide](https://kits.plusivo.com/wirelesskit/guide.zip), pagina 72

[MB102 Datasheet](https://www.handsontec.com/dataspecs/mb102-ps.pdf)

[KEETEC TS-10
Datasheet](https://alarmservice.ro/wp-content/uploads/images/products/products-ts10_instalare.pdf)

[Block Diagram Tool](https://app.diagrams.net)

[Hardware Design Tool](https://fritzing.org)

### Software

#### Arduino

[HC-05
Datasheet](https://components101.com/sites/default/files/component_datasheet/HC-05%20Datasheet.pdf)

[Biblioteca
SoftwareSerial](https://docs.arduino.cc/learn/built-in-libraries/software-serial)

[Biblioteca LCD_I2C](https://github.com/blackhack/LCD_I2C)

#### Android

[Material Theme
Builder](https://material-foundation.github.io/material-theme-builder)

[Material 3 UI Components](https://m3.material.io/components)

[Bluetooth
Setup](https://developer.android.com/develop/connectivity/bluetooth/setup)

[Find Bluetooth
devices](https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices)

[Connect Bluetooth
devices](https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices)

[Request runtime
permissions](https://developer.android.com/training/permissions/requesting)

[Comunicare de la Fragment la
Activity](https://www.youtube.com/watch?v=HkOnY3Lf9q0)