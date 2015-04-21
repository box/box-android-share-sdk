Box Android Share SDK
==============
This SDK enables the ability to easily manage share links and collaborators on Box.

Quickstart
--------------
The SDK can be obtained by adding it as a maven dependency, cloning the source into your project, or by downloading one of the precompiled JARs from the releases page on GitHub.

This SDK has the following dependencies and will need to be included if you use the JAR:
* minimal-json v0.9.1 (for maven: com.eclipsesource.minimal-json:minimal-json:0.9.1)
* box-content-sdk (for maven: )

Example:
--------------
    // You will need a BoxSession and the BoxItem from the box-content-sdk 
    // Please refer to the documentation on the box-content-sdk for additional details.
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
    
    // To launch the activity to manage share links:
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
    
    // To launch the activity to manage collaborators:
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
