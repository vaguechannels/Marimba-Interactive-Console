# Marimba Interactive Console (Page Still Under Construction!)


# **Summary**
  
The following document provides information on how to setup Marimba Interactive Console within the packages built on marimba that allows the end user the flexibility to determine when applicable software updates are applied to their machines up to a defined deadline.  
***
# **Preface**
  
Currently application packagers spend many hours on designing a user interface for any their software packages that requires some type of user approval mechanism prior to launch the installation of a packaged application. There were multiple scripts/exes and different versions of interface used which sometimes lead to misinterpretation from user’s context. Multiple process were spun up, some in user’s context which caused many prompts/dialog on the screen that caused control misplacement on software installation workflow.     
***
# **What is expected?**
  
At the endpoint level, if the Marimba Interactive Console is enabled within the package, upon the execution of the packaged channel, the user (if anyone logged in) will be prompted with the prompt for approval to proceed with the installation. User can chose to proceed with the installation or postpone deferring the installation to a later time.
***
                                                 PROCEED/POSTPONE PROMPT
***
If user decides to postpone the installation, they will be presented with below dialog box to choose to be reminded back for the installation. Currently users have the ability to postpone the installation to a maximum of 8 hours in addition to the total number of times they can postpone. The snooze counter decrements every time the user hits snooze after choosing the delay timer. 
***
                                                     SNOOZE PROMPT
***
The below dialog box will be skipped if the user chooses to proceed with the installation or when no one is logged on during the execution of the packaged channel even if the reminder set on the initial snooze has not expired yet. This feature allows the machines to just process any required policy updates when the machines are not currently in use(not logged on).
When the user chooses to proceed with the installation on the first prompt, a progress message is kicked off as a individual thread within the marimba agent to inform the user about the activity done on the system. While the progress message is up on the computer screen, the actual package installation will proceed in the background. Care must be taken for any pre-installation activities such as process kills, removal of current application, etc. for smoother execution of the package installation. 


