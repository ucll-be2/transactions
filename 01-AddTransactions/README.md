# Transactions - Oplossing probleem 1

We zullen eerst het eerste probleem oplossen: overschrijven naar een niet-bestaande account.
Hiervoor gaan we [`AccountService#transferFunds`](src/main/java/be/ucll/be2/transactions/service/AccountService.java)
aanpassen zodat de hele transfer in één transactie gebeurt. Dat betekent dat als er een fout optreedt,
de hele transfer wordt teruggedraaid en het geld niet verloren gaat.

Bij een transactie worden alle bewerkingen die binnen de transactie plaatsvinden, pas definitief doorgevoerd als de
transactie succesvol wordt afgesloten. Dit noemt men een ***commit***. Als er een fout optreedt, kunnen we alle bewerkingen
die binnen de transactie hebben plaatsgevonden, terugdraaien. Dit noemt men een ***rollback***.

## `@Transactional`

In Spring kunnen we een transactie starten door de annotatie
[`@Transactional`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/annotation/Transactional.html) te gebruiken. We voegen deze annotatie toe aan de methode [`AccountService#transferFunds`](src/main/java/be/ucll/be2/transactions/service/AccountService.java):

```java
@Transactional
public void transferFunds(long fromAccountId, long toAccountId, BigDecimal amount, boolean slow) {
    // Implementatie
}
```

Als je echter enkel `@Transactional` toevoegt zal je echter merken dat er nog steeds geen automatische *rollback*
gebeurt wanneer er een `AccountNotFoundException` wordt gegooid. Probeer maar eens opnieuw een overschrijving te doen
naar een niet-bestaande account. Je zal zien dat het geld nog steeds verdwijnt.

Dit komt door het standaard gedrag van Spring:

- Bij een *checked exception* (zoals `AccountNotFoundException`) wordt er **geen rollback** uitgevoerd.
- Bij een *unchecked exception* (zoals een `RuntimeException`) of bij een *error* wordt er wél een *rollback* uitgevoerd.

## `rollbackFor`/`noRollbackFor`

We zullen dus moeten uitdrukken dat we bij `AccountNotException` en `InsufficientFundsException` een *rollback* willen
uitvoeren. Dit kan door de annotatie `@Transactional` aan te passen:

```java
@Transactional(rollbackFor = {AccountNotFoundException.class, InsufficientFundsException.class})
public void transferFunds(long fromAccountId, long toAccountId, BigDecimal amount, boolean slow) {
    // Implementatie
}
```

Met `rollbackFor` geven we aan dat er een *rollback* moet gebeuren bij de opgegeven exceptions. Probeer nu opnieuw een
overschrijving te doen naar een niet-bestaande account. Je zal zien dat het geld niet meer verdwijnt.

Het tegenovergestelde kan ook: als je geen *rollback* wilt bij een bepaalde exception, kan je `noRollbackFor` gebruiken:

```java
@Transactional(noRollbackFor = {DontRollbackForThisException.class})
```

## Alternatief: exceptions veranderen in `RuntimeException`s

Je kan er ook voor kiezen om de exceptions `AccountNotFoundException` en `InsufficientFundsException` te laten erven van
`RuntimeException` in plaats van van `Exception`. Dan worden deze exceptions automatisch behandeld als
*unchecked exceptions* en zal er bij het gooien van deze exceptions automatisch een *rollback* gebeuren. Dan is het
niet meer nodig om `rollbackFor` te gebruiken.

Verschillende ontwikkelaars hebben verschillende voorkeuren: sommigen vinden het beter om *checked exceptions* te
gebruiken, wat hen dan eraan herinnert dat ze die exceptions moeten afhandelen, met dan het nadeel dat ze eraan moeten
denken om `rollbackFor` te gebruiken. Anderen vinden het beter om *unchecked exceptions* te gebruiken, zodat ze zich
daar geen zorgen over hoeven te maken, maar dan moeten ze wel opletten dat ze de exceptions correct afhandelen.

Je mag zelf kiezen welke aanpak je verkiest. In deze oplossing hebben we gekozen voor
*checked exceptions* met `rollbackFor`.
