# Transactions

De voorbeelden in dit demoproject tonen aan welke problemen er kunnen voorkomen
als er geen transacties gebruikt worden, en hoe transacties hierbij kunnen helpen.

- De [`bruno`](./bruno)-map bevat een Bruno-collectie met requests om het project te testen
- [`00-Buggy`](./00-Buggy) bevat de initiële versie van het project, die een aantal problemen vertoont
- [`01-AddTransactions`](./01-AddTransactions) voegt transacties toe, die automatisch terugdraaien (*rollback*) bij een probleem (exception)
- [`02-AddVersion`](./02-AddVersion) voegt een veldje toe dat de versie van de *entity* bijhoudt, om te vermijden dat de data in de database inconsistent wordt
