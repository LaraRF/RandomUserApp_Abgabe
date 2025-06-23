# RandomUserApp_Abgabe - ReadMe

Eine moderne Android-Anwendung für die Verwaltung von Benutzerdaten mit AR-Integration, entwickelt für den Kurs "VR, AR & Mobile Development" an der SRH Hochschule Heidelberg.

Features:
- Random User API Integration (Random User werden von https://randomuser.me/ bezogen) -> automatisches Laden von Zufallspersonen ("Random User")
- Lokale SQLite Dartenbank -> persistente Datenspeicherung
- AR/Kamera-Integration -> QR-Code basierte Augmented Reality
- Manuelle Benutzererstellung -> eigene Benutzer können erstellt und verwaltet werden
- Suche und Sortierung -> Sortierung nach Name (alphabetisch nach Vorname, a -> z), Datum (Erstellung, neuester User ganz oben), Geburtstag im Jahr (unabhängig von Jahrgang, erste Person im Jahr ganz oben (Person, die am nähesten am 1.1. Geburtstag hat)) oder nach Jahrgang (jüngste Person ganz oben) möglich
- Mehrsprachigkeit -> Standard ist Englisch, Deutsch möglich
- Dark-Mode -> Theme kann angepasst werden

Bonus Features:
- Dependency Injection -> Architektur mit Hilt
- Statistiken -> Datenbank-Statistiken einsehbar
- Settings-Management -> Dankenbank-Verwaltung (gezieltes Leeren und Befüllen) und App-Einstellungen
- QR-Code 
