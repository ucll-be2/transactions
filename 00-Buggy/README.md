# Transactions – Buggy

Dit is een kleine Spring Boot-applicatie om bankrekeningen te beheren.
Dit project bevat **bewust** twee bugs rond transacties en dient als vertrekpunt om te leren waarom
`@Transactional` (en eventueel locking) nodig is.

## Endpoints

| Methode | URL | Body | Beschrijving |
|---------|-----|------|--------------|
| GET | `/api/v1/accounts` | – | Alle accounts |
| GET | `/api/v1/accounts/{id}` | – | Eén account |
| POST | `/api/v1/accounts` | `{"owner": "..."}` | Nieuw account (saldo 0) |
| POST | `/api/v1/accounts/{id}/deposits` | `{"amount": 10}` | Geld storten |
| POST | `/api/v1/accounts/{id}/withdrawals` | `{"amount": 10}` | Geld opnemen |
| POST | `/api/v1/transfers` | `{"fromAccountId":1,"toAccountId":2,"amount":10}` | Overschrijving |

Bij `POST /api/v1/transfers` kan je in de body een extra veld `speed` meegeven. Standaard is
dit `FAST`. Als je `SLOW` meegeeft, wordt er in [`AccountService#transferFunds`](src/main/java/be/ucll/be2/transactions/service/AccountService.java) 10 seconden gewacht **na** het aftrekken van het bedrag van de bronaccount, maar **voor** dit wordt weggeschreven naar de database. Dit is handig om *race conditions* te simuleren.

## Probleem 1: overschrijven naar een niet-bestaande account

Probeer eens een overschrijving te doen naar een account die niet bestaat. Bijvoorbeeld:

1. Maak een nieuwe account aan:
   ```
   POST /api/v1/accounts {"owner":"Jos Bosmans"} -> id 1
   ```
2. Stort 100 euro op account 1:
   ```
   POST /api/v1/accounts/1/deposits {"amount":100}
   ```
3. Probeer 50 euro over te schrijven naar account 999 (die niet bestaat):
   ```
   POST /api/v1/transfers {"fromAccountId":1,"toAccountId":999,"amount":50}
   ```

Wat zie je? De server geeft een 404 terug, maar als je daarna het saldo van account 1 opvraagt, is het **50**. Het geld is verdwenen.

Hoe kan dit?

[`AccountService#transferFunds`](src/main/java/be/ucll/be2/transactions/service/AccountService.java) doet in volgorde:

1. Haal de bronaccount op (`getAccount(fromAccountId)`)
2. Trek het bedrag af van de bronaccount (`fromAccount.withdraw(amount)`)
3. Sla de bronaccount op (`accountRepository.save(fromAccount)`) ← **wordt al weggeschreven naar de database**
4. Haal de bestemmingsaccount op (`getAccount(toAccountId)`) ← **geeft een fout**

Het resultaat is dat in stap 3 het nieuwe saldo van de bronaccount al is weggeschreven naar de database, terwijl stap 4 een fout geeft omdat de bestemmingsaccount niet bestaat. De client krijgt een 404, maar het geld is **verdwenen**: het staat op geen enkele account meer. De database is inconsistent (de som van alle saldi is gedaald).

## Probleem 2: gelijktijdige transfer A → B en opname bij A (*lost update*)

Probeer eens het volgende:

- Maak twee accounts aan:
  ```
  POST /api/v1/accounts {"owner":"Jos Bosmans"} -> id 1
  POST /api/v1/accounts {"owner":"Clement Peerens"} -> id 2
  ```
- Stort 100 euro op account 1:
  ```
  POST /api/v1/accounts/1/deposits {"amount":100}
  ```
- Start een transfer van 50 euro van account 1 naar account 2, maar geef `speed=SLOW` mee:
  ```
  POST /api/v1/transfers {"fromAccountId":1,"toAccountId":2,"amount":50,"speed":"SLOW"}
  ```
- Terwijl de transfer bezig is (er wordt 10 seconden gewacht), doe een opname van 75 euro bij account 1:
  ```
  POST /api/v1/accounts/1/deposits {"amount":75}
  ```
- Wacht tot de transfer klaar is en vraag het saldo van account 1 op:
  ```
  GET /api/v1/accounts/1
  ```

Wat zie je? Beide bewerkingen lijken te slagen, maar het saldo van account 1 is 50 euro. Dat is fout: 100 − 50 (transfer) − 75 (opname) is niet mogelijk, want er stond maar 100 euro op. Eén van beide bewerkingen had geweigerd moeten worden.

Wat is er gebeurd?

1. De transfer leest account 1 (saldo 100), trekt er 50 van af in het geheugen en gaat 10 seconden slapen, nog zonder te hebben opgeslagen.
2. In die tijd leest de opname hetzelfde account (saldo nog steeds 100), trekt er 75 af en slaat 25 op.
3. Daarna wordt de transfer hervat en slaat zijn eigen kopie met saldo 50 op. Daarmee wordt de 25 van de opname **overschreven**.

Resultaat: account 2 kreeg 50 euro, de klant heeft 75 euro opgenomen, en toch is er maar 50 euro van account 1 afgegaan. Er is 75 euro uitbetaald die nergens in de boekhouding staat. Dit heet een *lost update*: twee requests werken tegelijk op een verouderde kopie van dezelfde rij, en de laatste `save` wint.