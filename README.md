# Vaadin-projekti (Prodeca)

> Projekti on toteutettu osana Java web-ohjelmointikurssia.

Prodeca on verkkopohjainen varastonhallintajärjestelmä tuotteiden seurantaan, toimittajien hallintaan ja ostotilausten tarkkailuun. Se käyttää seuraavia teknologioita: Java 21, Spring Boot, Vaadin Flow, PostgreSQL

---

## Esivaatimukset
- [Docker](https://docs.docker.com/get-docker/)

Mitään muuta ei tarvita, sillä kaikki suoritetaan Dockerin konteissa.

---

## Ohjelman ajaminen
### 1. Kloonaa projekti ja sitten navigoi seuraavaan hakemistoon:
```bash
cd Vaadin-projekti/prodeca
```

### 2. Käynnistä ohjelma ja tietokanta Docker Composella:
```bash
docker compose up --build
```

--build tarvitaan vain ensimmäisellä käynnistyskerralla. Seuraavilla kerroilla sen voi jättää pois.

### 3. Katso Dockerin log-tulostetta. Ohjelma on valmiina, kun näet samankaltaisen rivin:
```
prodeca_app | INFO --- com.prodeca.Application : Started Application in x.xxx seconds (process running for x.xxx)
```

### 4. Avaa selain osoitteessa **http://localhost:8080**

Kaksi käyttäjää on luotu automaattisesti, joilla voi käyttää ohjelmaa:
| Käyttäjänimi | Salasana | Rooli |
|---|---|---|
| `admin` | `admin123` | Admin |
| `user` | `user123` | User |

### 5. Ohjelma pysäytetään painamalla `Ctrl+C` terminaalissa, jossa Docker Compose on käynnissä ja sitten antamalla komento:
```bash
docker compose down
```

Voit myös poistaa tietokanta-volumen (poistaa kaiken tallennetun datan):
```bash
docker compose down -v
```