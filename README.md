# <a href="https://ocw.cs.pub.ro/courses/pm/prj2024/rvirtan/wireless-lock" style="color:inherit; text-decoration:none">Închidere centralizată wireless pentru autoturisme</a>

## Introducere

În prezent, majoritatea autoturismelor noi vin cu dotări precum keyless entry și posibilitatea de a-ți conecta mașina la smartphone-ul personal prin intermediul unei aplicații dedicate.

Proiectul presupune implementarea unui sistem de keyless entry controlat din telefon, cât și montarea acestuia pe o mașină (mașina personală) care nu vine dotată cu sistemele menționate mai sus. Aplicația de pe smartphone va permite încuierea mașinii, cât și verificarea stării închiderii centralizate (încuiat/descuiat).

De asemenea, în traficul bucureștean și nu numai, ești lovit de situația în care, după ce ai ajuns la destinație, găsești un loc de parcare care nu-ți aparține, așa că îți lași numărul de telefon "în parbriz" astfel încât să poți fi contactat de proprietar în caz că ajunge acasă și nu își găsește locul liber.

*Dar ce faci în cazul în care nu ai o hârtie sau pix ca să-ți treci numărul de telefon?*

Ei bine, pe lângă sistemul de keyless entry, îți vei putea seta din aplicație datele de contact (prenume și număr de telefon), astfel încât acestea vor fi afișate pe un display.

    Proiectul va fi dezvoltat în așa fel încât să permită adăugarea facilă de noi funcționalități.

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
-   Modul închidere centralizată stock;
-   Škoda Octavia 2, 1.9 TDI, an de fabricație 2007 (cazanul pe care voi monta sistemul).

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/hardware_design.png" class="align-center" width="600" />

Pentru buna funcționare a circuitului de mai sus, designul este alcătuit din:

-   circuitul modul HC-05, LCD și LED martor de armare, conectate la VCC-ul Arduino-ului (partea superioară a breadboard-ului);
-   circuitul buzzer-ului, care este conectat la VCC-ul modulului de alimentare HW-131 (partea inferioară a breadboard-ului).

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/circuit_buzzer.png" class="align-center" width="600" />

Am împărțit astfel, deoarece în circuitul buzzer-ului am folosit tranzistorul NPN 2N2222A pentru a amplifica curentul. Buzzerul pasiv este cunoscut pentru faptul că se aude încet, deci prin amplificarea curentului am reușit să măresc volumul buzzer-ului. Problema care apare aici este că amplificarea vine la pachet cu un consum mare de energie atât timp cât este pornit buzzer-ul (am fost lovit de situația în care LCD-ul "pâlpâia" pentru că nu avea suficient curent).

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/setup_hw_131.jpg" class="align-center" width="600" />

    După cum se poate observa în setup, partea superioară a sursei de alimentare este setată pe OFF (nu avem nevoie de curent acolo, deoarece am conectat deja VCC-ul și GND-ul de la Arduino), iar partea inferioară am setat-o pe 5V.

    Nu am inclus autoturismul în design (încă), deoarece încă nu e finalizat proiectul. Astfel, voi monta sistemul pe mașină abia după ce termin și partea software a proiectului. De asemenea, layout-ul final al circuitului va fi schimbat, mai exact pinii RX și TX de pe modulul de Bluetooth îi voi conecta direct la pinii TX și RX de pe Arduino (momentan am realizat o conexiune serială software pentru a avea acces la Serial Monitor).

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/circuit.jpg" class="align-center" width="600" />

<img src="https://ocw.cs.pub.ro/courses/_media/pm/prj2024/rvirtan/wireless-lock/lcd_date_contact.jpg" class="align-center" width="600" />

## Software Design

## Rezultate Obţinute

## Concluzii
