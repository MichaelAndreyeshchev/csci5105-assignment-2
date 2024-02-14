# Running Part A:
```
$ javac *.java

$ java BankServer <port>

$ java BankClient <hostname> <port> <threads> <iterations>
```
# Running Part B:
```
javac *.java

java RMIBankServerImp <port>

java RMIBankClient <hostname> <port> <threads> <iterations>