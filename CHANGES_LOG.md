29.11
- utworzenie projektu  

30.11
- Opracowanie koncepcji rozwijania projektu,  
- dodanie bazy PostgreSQL używając dockerowej konteneryzacji,  
- napisanie bazowych struktur projektu  

01.12
- wysyłanie eksperymentu z cli do serwera


07.12
- delikatne poprawienie jakosci kodu
- dodanie funkcji domyslnych parametrow
- dodatnie funkcji wykonywania ekspertymentu w zaleznosci od podanych przez uzytkownika parametrow
- dodanie readme.md
- dodanie funkcji zapisywania eksperymentu oraz odpowiednich metryk do bazy danych

08.12
- poprawa kodu związanego z wysyłaniem danych do bazy
- dodanie pobierania danych z bazy danych
- dodanie możliwości odbierania wyników eksperymentu po id z cli
- mapowanie danych z serwera i tworzenie tabelki z metrykami
- poprawa nazw endpointów

10.12
- dodanie automatycznego pobierana experymentów z serwera gdy zostaną zakończone

11.12.2024
- dodanie komendy get-all, która pobiera informacje o statusie wszystkich eksperymentów
- refactor kodu
- dodanie singletona CliConfig, gdzie zapisane są endpointy, z których korzystamy
- zmiana ScheduledExperimentFetcher tak zeby nie printował nam wyników na konsole tylko samą informację,że eksperment jest COMPLETED

12.12
- poprawa listowania eksperymentów – dodano możliwość filtrowania eksperymentów po statusie
- dodanie DTO dla eksperymentów by nie wysyłać niepotrzebnie metryk gdy użytkownik o to nie prosi