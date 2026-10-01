Group 49 - Typing Duel
Team Members: Atika Hussain, Misol Kang, Mrida Hingmire, Suntae Kim, and Zachary Barron


Software Description
Typing Duel is a Java-based desktop game that combines typing practice with fighting game mechanics.
Players engage in a duel with a computer-controlled enemy where they must type the words on-screen quickly and accurately to attack
and defend. Performance affects damage dealt, score, and game progression.
Key features include :
Three difficulty levels (Easy, Medium, and Hard), as well as an Endless mode that gets progressively harder.
Player accounts and stat tracking.
A high score system for each level.
Guardian/Parental controls.
An interactive GUI built with Java Swing.
Typing Duel aims to improve the users' typing capability through fun and engaging gameplay.


Required Libraries and Tools
2a. Core Requirements
2ai. Java JDK 19
2aii. Windows 11 Operating System
2b. Libraries
2bi. Gson 2.10
2c. Tools
2ci. Java compiler (javac)
2cii. Java runtime (java)


Build Guide
Step 1. Install Java
1a. Download JDK from https://adoptium.net/ or Oracle
1b. Install Java JDK 19
1c. Verify installation through the Windows Terminal (or other similar software like Notepad or Command Prompt)
1ci. Run the following commands:
java -version
javac -version
1cii. If the commands produce an output similar to:
openjdk version "19.0.15" 2025-04-15
OpenJDK Runtime Environment Temurin-17.0.15+6 (build 19.0.15+6)
OpenJDK 64-Bit Server VM Temurin-17.0.15+6 (build 19.0.15+6, mixed mode, sharing)
1ciii. For each of the commands, then they have been installed correctly
Step 2. Ensure Project Structure
2a. You must have:
src/
lib/gson.jar
data/
resources/
Step 3. Clean Old Build
3a. Use the 'cd Typing-Duel-Template' command to navigate to the project root
3b. Use the following commands to ensure proper compilation:
Remove-Item -Recurse -Force out
mkdir out
3c. Compile the program by entering 'javac -cp "lib/gson.jar" -d out -sourcepath src src\backend\Main.java
3d. Verify the compilation by entering 'out/backend/Main.class'
If it exists, compilation succeeded.


Run Guide
5a. From the project root (Typing-Duel-Template), enter 'java -cp "out;lib/gson.jar;resources" backend.Main'


User Guide

Launch the program
Register a new account or log in through the Login button
Once logged in, select the Play button, and then select a level to begin gameplay
Type the displayed words in time to either Attack and deal damage, or Defend and block damage
If you complete three successful attacks in a row, Flurry Rush will activate, and your next attack will do double damage
If you complete three successful defenses in a row, Brick Wall will activate, and your next defense will reflect incoming damage
Difficulty increases as you progress through the levels. Difficulty is defined by the word complexity.
To see your typing statistics, select the High Scores and Statistics button.
The Settings button can be selected to alter volume, brightness, and enable colourblind accessibility.
The tutorial can be accessed from the Tutorial button
When you are done playing, select the Logout button to return to the Main Menu, and then select the Exit button to exit the game.



Account Credentials
In order to play Typing Duel, you must create an account, your account will require a unique Username, Password, and Security Question
to be entered, as well as a Guardian Code if the account is a Guardian/Parental Control account.
The Username and Password are what is used to log in on repeat gameplay sessions, and the Security Question response is used to
recover your password if you forget.


Parental Controls
Parental controls can be accessed from the Main Menu screen, if you do not yet have a Guardian account, you can register for one,
and if you do, then you can log in. Once logged in, you will be able to view statistics for each user, including Accuracy%, time
played, high scores, and more. As a Guardian, you can choose to reset the High Scores for everyone, reset a certain players account,
or simply view statistics.


Additional Information
8a. File Types Used
8ai. .json
Used for configurations, account persistence, and statistics
8aii. .csv
Used for word lists
8aiii. .ttf
Used for fonts
8aiv. .png/.jpg
Used for graphics
8av. .wav
Used for audio