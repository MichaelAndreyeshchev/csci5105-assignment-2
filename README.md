<<<<<<< HEAD
# Running Part A: 3
=======
Group members: Michael and Wyatt (andr0821 & rasmu984)

# Running Part A:
>>>>>>> 32d53a4b7581481a2b2d0ef821c7b6d849672688
```
$ javac *.java

$ java BankServer <port>

$ java BankClient <hostname> <port> <threads> <iterations>
```
# Running Part B (NO 'rmiregistry' COMMAND REQUIRED):
```
javac *.java

java RMIBankServerImp <port>

java RMIBankClient <hostname> <port> <threads> <iterations>
```
# Logging:
Note that all UIDs must be non negative and greater than 0.  Thus, in the log files when we do not do the transfer operation, we set Target Account UID: -1 because we are not using it.