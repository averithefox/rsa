# How to setup:  

## Prerequisites (what i use)
### Project Structure 
1: ms-21 (Or JDK-25)

2: Language level -> SDK default

### Gradle JVM
ms-21 (Or JDK-25) 
## .


## Step 1
Clone RSM from github

## Step 2
```bash
./gradlew build
```
If you're using IntelliJ just use the buttons.

After Building you want to save that copy of RSM. It should be found in the folder: rsmodern -> build -> libs -> rsm-#.#.#.jar
This .jar will be used for RSA.

## Step 3
```bash
./gradlew publishToMavenLocal
```

Now RSM forge is in your local maven, you can access all its classes when you are coding RSA.


## Step 4
Copy RSA from github

### In RSA Project.

Once gradle configures, Redo the Prerequisites in RSA the same way you have it in RSM.

Once you've done that go the "mods" folder and drop RSM, and Fabric-Api into it. (rsamodern -> run -> mods)

## Step 5.
Reload Gradle and attempt to launch your game. If it gives you an error, then report the error. Otherwise, If you loaded up fine then you should be able to code away!


# End
### Each time you update your RSM / RSA version(for the rsa version its only if both need to be updated) you need to pull.

### Then redo Steps 1-3 and redrag your rsm.jar into the mods folder in your RSA project for the errors in rsa to go away.
