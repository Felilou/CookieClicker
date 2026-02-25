# 🍪 CookieClicker Frontend

Ein modernes Frontend mit Login, Registrierung und geschützter Stats-Seite.

## 📦 Features

- ✅ **Login** - User-Authentifizierung mit JWT
- ✅ **Registrierung** - Neue User anlegen
- ✅ **Logout** - Session beenden
- ✅ **Stats-Seite** - Zeigt Gesamtanzahl der Games (nur für authentifizierte User)
- ✅ **Responsive Design** - Funktioniert auf allen Geräten
- ✅ **Moderne UI** - Gradient Design mit Animationen

## 🚀 Starten

### Voraussetzungen

- Backend Services müssen laufen:
  - **Auth-Service** auf Port 8081
  - **Persistence-Service** auf Port 8082

### Option 1: Mit Python HTTP Server

```powershell
cd C:\Users\Derec\IdeaProjects\CockieClicker\frontend
python -m http.server 3000
```

Öffne: http://localhost:3000/login.html

### Option 2: Mit Node.js http-server

```powershell
cd C:\Users\Derec\IdeaProjects\CockieClicker\frontend
npx http-server -p 3000
```

Öffne: http://localhost:3000/login.html

### Option 3: Mit Live Server (VS Code Extension)

1. Installiere "Live Server" Extension in VS Code
2. Rechtsklick auf `login.html`
3. "Open with Live Server"

## 📱 Seiten

### login.html
- Login-Formular
- Link zur Registrierung

### signup.html
- Registrierungs-Formular
- Felder: Username, Email, Vorname, Nachname, Passwort
- Link zum Login

### stats.html
- **Geschützte Seite** - Nur mit JWT-Token zugänglich
- Zeigt Gesamtanzahl der Games aus der Datenbank
- Navbar mit Username und Logout-Button
- Automatische Weiterleitung zum Login wenn nicht authentifiziert

## 🔐 Authentifizierung

Die Authentifizierung erfolgt über JWT Tokens:

1. **Login/Registrierung** → JWT Token von Auth-Service
2. **Token wird in LocalStorage gespeichert**
3. **Jeder API-Call** → Token im Authorization Header
4. **Logout** → Token wird aus LocalStorage gelöscht

## 🎨 Design

- **Gradient Background** - Lila/Blau
- **Weiße Cards** - Mit Shadow-Effekt
- **Responsive** - Mobile-First Design
- **Animationen** - Smooth Transitions & Counter-Animation

## 🛠️ Dateien

```
frontend/
├── login.html          # Login-Seite
├── signup.html         # Registrierungs-Seite
├── stats.html          # Stats-Seite (geschützt)
├── auth.js             # Auth-Logik (login, signup, logout)
├── styles.css          # Styling
└── README.md           # Diese Datei
```

## 📡 API Calls

### Auth-Service (Port 8081)

```javascript
// Login
POST /auth/login
Body: { username, password }
Response: { access_token, ... }

// Registrierung
POST /auth/register
Body: { username, email, password, firstName, lastName }
Response: { username, message }
```

### Persistence-Service (Port 8082)

```javascript
// Stats abrufen
GET /api/stats
Headers: { Authorization: "Bearer <token>" }
Response: { totalGames: 42 }
```

## 🧪 Testen

1. **Starte Backend Services:**
   ```powershell
   # Terminal 1
   cd auth-service
   mvn spring-boot:run
   
   # Terminal 2
   cd persistance-service
   mvn spring-boot:run
   ```

2. **Starte Frontend:**
   ```powershell
   cd frontend
   python -m http.server 3000
   ```

3. **Öffne Browser:**
   - http://localhost:3000/login.html

4. **Registriere einen User:**
   - Klicke auf "Jetzt registrieren"
   - Fülle Formular aus
   - Nach erfolgreicher Registrierung → Weiterleitung zum Login

5. **Login:**
   - Username und Passwort eingeben
   - Nach erfolgreichem Login → Weiterleitung zu Stats

6. **Stats ansehen:**
   - Siehst Gesamtanzahl der Games
   - Logout-Button oben rechts

## 🐛 Troubleshooting

### CORS Fehler
- Stelle sicher, dass CORS in beiden Backend-Services aktiviert ist
- Prüfe ob `CorsConfig.java` in beiden Services vorhanden ist

### "401 Unauthorized" bei Stats
- Token ist abgelaufen → Neu einloggen
- Token ist ungültig → LocalStorage leeren und neu einloggen

### Backend nicht erreichbar
```powershell
# Prüfe ob Services laufen
curl http://localhost:8081/auth/login -X POST
curl http://localhost:8082/api/stats
```

### LocalStorage leeren
```javascript
// In Browser Console
localStorage.clear();
```

## 🎯 Nächste Schritte

- [ ] Token Refresh implementieren
- [ ] Error Handling verbessern
- [ ] Loading States verfeinern
- [ ] Weitere Stats hinzufügen
- [ ] User Profile Page
- [ ] Game-Management (CRUD)

---

**Viel Spaß! 🍪**

