# ModularProject
Project used has a base for a article using Android Studio and Java, with diverses APIs to create a modular approach to a software, in this case a test/questionary maker, where you can edit this base and have your own software.

# 1 Instructions

# 1.1 Prerequisites

Android Studio 2.X  
Android Developer Account (for releasing in play store only)

# 1.2 Initial Setup

Sync gradle in Android Studio  
Case "R" is a unknown symbol rebuild the project  
Change the package name to your own, in the AndroidManifest.xml file.  
(optional) Create a game service for this app, follow this link https://developers.google.com/games/services/console/enabling  
(optional) Replace the Achievments and Leaderboard values in strings.xml in Android Studio->QuestionaryMaker\src\main\res\values  


# 1.3 Create Tests

Use the template.json at the root folder, editing the fields, with any text (if the text are long, its advised to test them has they can be cropped in the app), take care to not break the file structure  
Convert the new file to a Base64 format, using any kind of software or webpage, e.G: base64encode.org  
Repeat has many times you need

# 1.4 Edit Extra Assets (Sounds)

Replace the files but maintain their names and formats, if you have a new click sound, rename it to "buttonSound.wav" and paste over the old one.  
In case its necessary to change the sound format, go to MainActivity.java in Android Studio and find the file name inside the code and edit to match your file, beware that some formats aren't acceptable.  

# 1.5 Change Layout 

In Android Studio, find the Activity and Fragments files in QuestionaryMaker\src\main\res\layout, for ease of use you can switch to the designer tab and edit how you want, just beware to not delete the necessary fields.  
You can change color, font, font size, Total space of each text.

# 1.6 Distribution

In case you didn't use the game services, you need to get your app id.  
All the steps are covered here, https://support.google.com/googleplay/android-developer/answer/113469?hl=en



