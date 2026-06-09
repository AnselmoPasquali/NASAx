# NASAx – Astronomy Explorer

Applicazione Android per esplorare il cosmo tramite le API ufficiali NASA.

## Componenti del gruppo

| Nome | Cognome | Matricola |
|------|---------|-----------|
| Anselmo | Pasquali | 886550 |

## Struttura del repository

```
NASAx/
├── app/                        # Codice sorgente Android
├── documentazione/
│   ├── Documentazione NASAx.pdf
│   └── Presentazione NASAx.pdf
├── screenshot/
│   ├── Home.png
│   ├── Apod.png
│   ├── Archive.png
│   ├── Explore.png
│   ├── Favorites.png
│   ├── News.png
│   ├── Quiz.png
│   └── Apod_Image_Big_Picture.png
└── README.md
```

## Descrizione

NASAx è un'applicazione mobile Android sviluppata interamente in Java che integra le API ufficiali NASA per offrire un'esperienza quotidiana dedicata all'astronomia.

**Funzionalità principali:**
- APOD (Astronomy Picture of the Day) con descrizione scientifica
- Archivio mensile sfogliabile dal 1995 ad oggi
- Esplorazione casuale di immagini NASA (Explore)
- Gestione preferiti con sincronizzazione Firebase Firestore
- Quiz astronomico con statistiche
- Feed notizie NASA
- Widget per la home screen e notifiche giornaliere automatiche (09:00)

**Stack tecnologico:**
- Java 17 · Android SDK 35 · minSdk 27
- MVVM + Repository Pattern · Dagger Hilt
- Room (SQLite) · Retrofit + Moshi · OkHttp · Coil
- Firebase Authentication (Google Sign-In) · Firebase Firestore
- WorkManager · NASA APOD API · Open Trivia Database
