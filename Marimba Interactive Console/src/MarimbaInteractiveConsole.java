/*
 * Credits		
 * 
 * DEBUG - Rakesh Dash // Harman
 * userSelectionDone - Bharath Mohanraj // Harman
 * ForceInstallOn - Jody Nemeth // DSG
 * BUTTON_PROCEED/BUTTON_POSTPONE/ShutdownTool - Kevin Elwell // DSG
 * 
 */

import java.util.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

//Importing Swing Components
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;

//Importing Marimba Components
import com.marimba.intf.application.*;
import com.marimba.intf.castanet.IActive;
import com.marimba.intf.castanet.IChannel;
import com.marimba.intf.castanet.ILauncher;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.intf.packager.*;
import com.marimba.intf.util.*;

public class MarimbaInteractiveConsole extends JFrame implements ActionListener, ItemListener, IScript{
	
	//Marimba Objects
	protected static IApplicationContext ctx;
	protected static IConfig tunerConfig;		//To Access Tuner Properties
	protected static IConfig config;			//To Access Channel Parameters
	protected static ILauncher launcher;		//To Access Channel Operations
	protected static IWorkspace workspace;		//To Access Tuner Workspace
	protected static IConfig chConfig;			//To Access Channel Properties
	
	//Global Variables
	protected static String ForceInstallOn, APPLICATION_NAME, APPLICATION_VERSION, FRAME_TITLE, phase;
	protected static long CURRENT_EPOCH = System.currentTimeMillis()/1000;
	protected static boolean DEBUG, userSelectionDone = false;
	protected static File CHANNEL_DIRECTORY;
	protected static int seconds = 0;
	
	//Prompt Variables
	protected static String PROMPTMESSAGE_TIMETAKEN, PROMPTMESSAGE_RESTART;
	protected static String background = "/scripts/background.jpg";
	protected static JButton BUTTON_PROCEED, BUTTON_POSTPONE, BUTTON_SNOOZE;
	protected static JLabel PROMPTMESSAGE_BACKGROUNDLABEL;
	protected static JComboBox SNOOZE_LIST = new JComboBox();
	protected static int SNOOZE_INDEX;
	
	//Progress Variables
	protected static String PROGRESSMESSAGE_HEADER, PROGRESSMESSAGE_BAR, PROGRESSMESSAGE_FOOTER;
	
	//Completed Variables
	protected static String COMPLETEDMESSAGE_HEADER, COMPLETEDMESSAGE_BAR;
	protected static boolean TIMER_EXPIRED = false;
	protected static int TIMER_COMPLETION = 1;	//Value In Minutes
	protected static Timer TIMER;
	
	//Shared Progress & Completed Variables
	protected static JProgressBar progressBar;
	protected static JTextField jtext01,jtext02;
	protected static JLabel labelleft, labelright;
	protected static String logo = "/scripts/logo.png";
	
	//Reboot Variables
	protected static String REBOOTMESSAGE_COMPLETE, REBOOTMESSAGE_RESTARTREQUIRED, REBOOTMESSAGE_SAVEWORK;
	protected static int TIMER_REBOOT = 5;	//Value In Minutes
	
	//ProcessKill Variables 
	protected static final String TASKLIST = "tasklist";
	protected static final String KILL = "taskkill /F /T /IM ";
	protected static ArrayList<String> LIST_PROCESS = null;
	   
	//MarimbaChannelManager Variables
	protected static String CHANNEL_LIST, CHANNEL_ARGUMENTS, CHANNEL_TIMEOUT;
	protected static String CHANNEL_STATUS = "installed";
	protected static String ACTION_REMOVE = "-remove";
	protected static String ACTION_INSTALL = "-install";
	protected static String CHANNEL_VALIDATION;
	protected static ArrayList<String> LIST_CHANNELURLS, LIST_CHANNELARGS = null;	
	protected static long to = 600000;	//Defaulting CHANNEL_TIMEOUT - 10 Minutes/600000 MillSeconds
	
	public MarimbaInteractiveConsole() {}
	
	@Override
	public int invoke(IScriptContext context, String[] args) 
	{
		ctx = (IApplicationContext) context.getFeature("context");
		launcher = (ILauncher) ctx.getFeature("launcher");
		workspace = (IWorkspace) ctx.getFeature("workspace");
		tunerConfig = (IConfig) ctx.getFeature("config");
		config = ctx.getConfiguration();
		chConfig = workspace.getChannelCreate(this.ctx.getChannelURL().toString());
		
		CHANNEL_DIRECTORY = new File(ctx.getDataDirectory().substring(0, ctx.getDataDirectory().length() - 5));
		if (DEBUG) System.out.println(DebugInfo() + "Channel Directory " + CHANNEL_DIRECTORY);
	
		switch (context.getPhase()) 
		{			
			case	IScriptContext.SCRIPT_PREINST:				
					phase = "Pre-Install";			
					break;
			case	IScriptContext.SCRIPT_PREREPAIR:			
					phase = "Pre-Repair";			
					break;
			case	IScriptContext.SCRIPT_PREUPDATE:			
					phase = "Pre-Update";			
					break;
			case	IScriptContext.SCRIPT_PREUPDATEMINOR:			
					phase = "Pre-MinorUpdate";		
					break;
			case	IScriptContext.SCRIPT_PREUNINST:			
					phase = "Pre-Uninstall";		
					break;
		/**********************************************************************************************/	
			case	IScriptContext.SCRIPT_POSTINST: 			
					phase = "Post-Install";			
					break;
			case	IScriptContext.SCRIPT_POSTREPAIR: 			
					phase = "Post-Repair";			
					break;
			case	IScriptContext.SCRIPT_POSTUPDATE:			
					phase = "Post-Update";			
					break;
			case	IScriptContext.SCRIPT_POSTUPDATEMINOR:		
					phase = "Post-MinorUpdate";		
					break;
			case	IScriptContext.SCRIPT_POSTUNINST:			
					phase = "Post-Uninstall";		
					break;
		}
		
		//Print Debug related messages
		if (	tunerConfig.getProperty("marimba.interactiveconsole.debug.enabled")!= null && 
				tunerConfig.getProperty("marimba.interactiveconsole.debug.enabled").equals("true")) {	DEBUG = true;	}
		
		if (DEBUG) System.out.println(DebugInfo() + "Debug flag enabled, So we will print all debug messages");
		
		System.out.println(DebugInfo() + "Channel running in " + phase + " phase");
		String argv[] = (String[])args;
        System.out.println(DebugInfo() + "Arguments Recieved " + argv[0]);
        
        if (phase != null)		
		{
        	try 
			{
        		//Initializing Variables
        		initializeVariables();
        		
				/********************************** PROMPT **********************************/
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
				
				//Proceed or Postpone Prompt
				if (argv[0].toUpperCase().equals("-PROMPT"))
		        {	
					//No Logged On User
					if(System.getProperty("user.name").endsWith("$"))
					{
						System.out.println(DebugInfo() + "No LoggedOn User: Installation will begin........");
						setParameter("marimba.interactiveconsole.exitcode", "0");
						setParameter("marimba.interactiveconsole.reboot.required", "true");
						if(getParameter("marimba.interactiveconsole.terminateprocess.enabled").equals("true"))	killProcess();	//Process Kills
					}
					
					//User Logged On & Pre-Validation Successful (If Channel Manager Enabled)
					if(!System.getProperty("user.name").endsWith("$") && getParameter("marimba.interactiveconsole.channelmanager.enabled").equals("true") && MarimbaChannelManagerValidation())
					{
						System.out.println(DebugInfo() + "All Dependant Channels are in expected state. Marimba Channel Manager Validation Successfull.");
						setParameter("marimba.interactiveconsole.exitcode", "0");
						setParameter("marimba.interactiveconsole.reboot.required", "false");    						
					}
					
					//User Logged On & Pre-Validation Failed(If Channel Manager Enabled)
					if((!System.getProperty("user.name").endsWith("$") && getParameter("marimba.interactiveconsole.channelmanager.enabled").equals("true") && !MarimbaChannelManagerValidation())
							|| 	(!System.getProperty("user.name").endsWith("$") && !getParameter("marimba.interactiveconsole.channelmanager.enabled").equals("true"))
							||	(!System.getProperty("user.name").endsWith("$") && getParameter("marimba.interactiveconsole.terminateprocess.enabled").equals("true")))
					{
						if(getParameter("marimba.interactiveconsole.channelmanager.enabled").equals("true") && !MarimbaChannelManagerValidation())	
						{	System.out.println(DebugInfo() + "Marimba Channel Manager Pre-Validation Failed. One or more dependant channels are not in expected state.");	}	
						if(((Long.valueOf(getParameter("marimba.interactiveconsole.forceinstall.active")) - CURRENT_EPOCH) > 0 || !getParameter("marimba.interactiveconsole.forceinstall.enabled").equals("true")))
						{
							if((CURRENT_EPOCH - Long.valueOf(getParameter("marimba.interactiveconsole.prompt.next")))>0)
							{
								if (DEBUG) System.out.println(DebugInfo() + "Value of userSelectionDone before starting Proceed/Postpone Frame: " + userSelectionDone);
								if (DEBUG) System.out.println(DebugInfo() + "Starting Proceed/Postpone Frame");
								
								MarimbaInteractiveConsole POPFrame = new MarimbaInteractiveConsole(FRAME_TITLE, APPLICATION_NAME, APPLICATION_VERSION, Boolean.valueOf(getParameter("marimba.interactiveconsole.forceinstall.enabled")),Boolean.valueOf(getParameter("marimba.interactiveconsole.proceed.enabled")),Boolean.valueOf(getParameter("marimba.interactiveconsole.postpone.enabled")));
								POPFrame.setVisible(true);
								
								setParameter("marimba.interactiveconsole.prompt.last" , Long.toString(CURRENT_EPOCH));
								setParameter("marimba.interactiveconsole.prompt.count" , Integer.toString(Integer.parseInt(getParameter("marimba.interactiveconsole.prompt.count"))+1));

								System.out.println(DebugInfo() + "Waiting On User's Choice.");
								while(!userSelectionDone)Thread.sleep(3000);
								System.out.println(DebugInfo() + "Proceeding with User's Choice...."); 
								if(!getParameter("marimba.interactiveconsole.button.clicked").equals("proceed"))	return Integer.parseInt(getParameter("marimba.interactiveconsole.exitcode"));
								if(getParameter("marimba.interactiveconsole.terminateprocess.enabled").equals("true"))	killProcess();	//Process Kills
							}
							else
							{
								setParameter("marimba.interactiveconsole.exitcode", "1");
								setParameter("marimba.interactiveconsole.reboot.required", "false");
								System.out.println(DebugInfo() + "Failing channel since next prompt time not reached. We will prompt in " +  (Long.valueOf(getParameter("marimba.interactiveconsole.prompt.next")) - CURRENT_EPOCH)/60  + " Minutes");
								return Integer.parseInt(getParameter("marimba.interactiveconsole.exitcode"));
							}
						}
						else 
						{
							setParameter("marimba.interactiveconsole.exitcode", "0");
							setParameter("marimba.interactiveconsole.reboot.required", "true");
							System.out.println(DebugInfo() + "Mandatory installation time reached. Installation will begin........"); 
							if(getParameter("marimba.interactiveconsole.terminateprocess.enabled").equals("true"))	killProcess();	//Process Kills
						}
					}
					 	
					if(getParameter("marimba.interactiveconsole.exitcode").equals("0"))
					{	
						//Marimba Channel Manager
    					if(getParameter("marimba.interactiveconsole.channelmanager.enabled").equals("true") && !MarimbaChannelManagerValidation())		
    					{	setParameter("marimba.interactiveconsole.exitcode", Integer.toString(MarimbaChannelManager()));	}
					}						
					//Main Exit Code
					return Integer.parseInt(getParameter("marimba.interactiveconsole.exitcode"));		
		        }
				
				/************************************* COMPLETED *************************************/
		        //Completion Dialog
		        if (argv[0].toUpperCase().equals("-COMPLETED"))
		        {
	        		//Verify if user is logged on
					if(!System.getProperty("user.name").endsWith("$"))
					{
						System.out.println(DebugInfo() + "Retrieving the Progress Message Dialog, so we can close it");			        	
						final Window[] allOpenProgressMessageWindows = Window.getWindows();
			        	if (allOpenProgressMessageWindows != null)
			        	{
			        	    for (final Window window : allOpenProgressMessageWindows)
			        	    {
			        	        if(window.toString().contains(FRAME_TITLE))	
			        	        {
			        	        	System.out.println(DebugInfo() + "Found Progress Message Dialog:" + window);
				        	        window.dispose();
				        	        System.out.println(DebugInfo() + "Progress Message Dialog Disposed");
			        	        }			        	        
			        	    }
			        	}
			        	
			        	UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			        	
						System.out.println(DebugInfo() + "Launching Completion Dialog");
						MarimbaInteractiveConsole CD = new MarimbaInteractiveConsole(FRAME_TITLE, APPLICATION_NAME, APPLICATION_VERSION, TIMER_COMPLETION);
						CD.setVisible(true);
						TIMER = new Timer(1000, CD);
						TIMER.start();
						if (DEBUG) System.out.println(DebugInfo() + "Value of TIMER_EXPIRED before Launching Completion Dialog: " + TIMER_EXPIRED);
						while(!TIMER_EXPIRED)Thread.sleep(3000);
						
						System.out.println(DebugInfo() + "Retrieving the Completion Dialog Frame, so we can close it");			        	
						final Window[] allOpenCompletionDialogWindows = Window.getWindows();
			        	if (allOpenCompletionDialogWindows != null)
			        	{
			        	    for (final Window window : allOpenCompletionDialogWindows)
			        	    {
			        	        if(window.toString().contains(FRAME_TITLE))	
			        	        {
			        	        	System.out.println(DebugInfo() + "Found Completion Dialog:" + window);
				        	        window.dispose();
				        	        System.out.println(DebugInfo() + "Completion Dialog Disposed");
			        	        }			        	        
			        	    }
			        	}			        	
			        	return 0;
					}
					else 
					{
						System.out.println(DebugInfo() + "No LoggedOn User: Supressing Progress dialog.");
						return 0;
					}
		        }
		        
		        /************************************* REBOOT *************************************/
		        //Reboot via ShutdownTool.exe 
		        if (argv[0].toUpperCase().equals("-REBOOT"))
		        {
	        		//Verify if user is logged on
					if(!System.getProperty("user.name").endsWith("$") && getParameter("marimba.interactiveconsole.reboot.required").equals("true"))
					{
						System.out.println(DebugInfo() + "Retrieving the Progress Message Dialog, so we can close it");			        	
						final Window[] allOpenProgressMessageWindows = Window.getWindows();
			        	if (allOpenProgressMessageWindows != null)
			        	{
			        	    for (final Window window : allOpenProgressMessageWindows)
			        	    {
			        	        if(window.toString().contains(FRAME_TITLE))	
			        	        {
			        	        	System.out.println(DebugInfo() + "Found Progress Message Dialog:" + window);
				        	        window.dispose();
				        	        System.out.println(DebugInfo() + "Progress Message Dialog Disposed");
			        	        }			        	        
			        	    }
			        	}
			        	//At this point the Progress Message Dialog should have been disposed
			        	if(config.getProperty("marimba.interactiveconsole.reboot.timer") != null) 
			    		{	
			    			if (DEBUG) System.out.println(DebugInfo() + "Retrieved Reboot Timer is " + config.getProperty("marimba.interactiveconsole.reboot.timer") + " Minutes");	
			    			TIMER_REBOOT = Integer.parseInt(config.getProperty("marimba.interactiveconsole.reboot.timer"));				
			    		}
			    		return shutdowntool(TIMER_REBOOT);			
			    	}
					else if (getParameter("marimba.interactiveconsole.reboot.required").equals("false"))
					{
						System.out.println(DebugInfo() + "No Reboot Required: Supressing all Marimba Interactive Console dialogs.");	return 0;
					}
					else 
					{	
						System.out.println(DebugInfo() + "No LoggedOn User: Supressing all Marimba Interactive Console dialogs.");	return 0;	
					}		    		
		        }
		        //If Incorrect Argument Passed, Script Fails
		        else channelUsage(); return -1;	
		    } 
			catch (Exception e) {e.printStackTrace();}
		}        
        System.out.println(DebugInfo() + "Abnormal exiting from invoke loop");
		return -1;        	
	}
	
	protected void initializeVariables()
	{
		//Initialize Variables
		if (DEBUG) System.out.println(DebugInfo() + "Initializing Variables..............");
		
		if(config.getProperty("marimba.interactiveconsole.application.name") != null){
			APPLICATION_NAME = config.getProperty("marimba.interactiveconsole.application.name");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized APPLICATION_NAME : " + APPLICATION_NAME);}
		
		if(config.getProperty("marimba.interactiveconsole.application.version") != null){
			APPLICATION_VERSION = config.getProperty("marimba.interactiveconsole.application.version");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized APPLICATION_VERSION : " + APPLICATION_VERSION);}
		
		FRAME_TITLE = "Software Update: " + APPLICATION_NAME + " " + APPLICATION_VERSION;
		
		/*************************************Resetting Prompt Message Variables*************************************/
		
		PROMPTMESSAGE_TIMETAKEN = "This update will take approximately 30-45 minutes to complete. All " + APPLICATION_NAME 
				+ " related applications will be shutdown during the update and a reboot will occur once completed. We strongly recommend to save all your work before proceeding with the updates.";
		
		PROMPTMESSAGE_RESTART = "NOTE: Once the update has started, please do not open any " + APPLICATION_NAME 
				+ " based applications or shutdown/restart your computer. The system will let you know once the update is complete and ready for a reboot.";
		
		if(config.getProperty("marimba.interactiveconsole.promptmessage.timetaken") != null){
			PROMPTMESSAGE_TIMETAKEN = config.getProperty("marimba.interactiveconsole.promptmessage.timetaken");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROMPTMESSAGE_TIMETAKEN : " + PROMPTMESSAGE_TIMETAKEN);}
		
		if(config.getProperty("marimba.interactiveconsole.promptmessage.restart") != null){
			PROMPTMESSAGE_RESTART = config.getProperty("marimba.interactiveconsole.promptmessage.restart");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROMPTMESSAGE_RESTART : " + PROMPTMESSAGE_RESTART);}
		
		/*************************************Resetting Progress Message Variables*************************************/
		
		PROGRESSMESSAGE_HEADER = APPLICATION_NAME + " " + APPLICATION_VERSION + ": Update is in progress.";
    	PROGRESSMESSAGE_BAR = "Please do not access " + APPLICATION_NAME + " or shutdown/restart your computer";
    	PROGRESSMESSAGE_FOOTER = "The system will prompt you to reboot once the update is complete."; 
		
		if(config.getProperty("marimba.interactiveconsole.progressmessage.header") != null){
			PROGRESSMESSAGE_HEADER = config.getProperty("marimba.interactiveconsole.progressmessage.header");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROGRESSMESSAGE_HEADER : " + PROGRESSMESSAGE_HEADER);}
		
		if(config.getProperty("marimba.interactiveconsole.progressmessage.bar") != null){
			PROGRESSMESSAGE_BAR = config.getProperty("marimba.interactiveconsole.progressmessage.bar");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROGRESSMESSAGE_BAR : " + PROGRESSMESSAGE_BAR);}
		
		if(config.getProperty("marimba.interactiveconsole.progressmessage.footer") != null){
			PROGRESSMESSAGE_FOOTER = config.getProperty("marimba.interactiveconsole.progressmessage.footer");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROGRESSMESSAGE_FOOTER : " + PROGRESSMESSAGE_FOOTER);}
		
		/*************************************Resetting Completion Dialog Message Variables*************************************/
		
		COMPLETEDMESSAGE_HEADER = APPLICATION_NAME + " " + APPLICATION_VERSION + ": Update is complete.";
		COMPLETEDMESSAGE_BAR = "Please reboot your machine if prompted before accessing " + APPLICATION_NAME;					
		
		if(config.getProperty("marimba.interactiveconsole.completedmessage.header") != null){
			COMPLETEDMESSAGE_HEADER = config.getProperty("marimba.interactiveconsole.completedmessage.header");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROGRESSMESSAGE_HEADER : " + COMPLETEDMESSAGE_HEADER);}
		
		if(config.getProperty("marimba.interactiveconsole.completedmessage.bar") != null){
			COMPLETEDMESSAGE_BAR = config.getProperty("marimba.interactiveconsole.completedmessage.bar");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized PROGRESSMESSAGE_BAR : " + COMPLETEDMESSAGE_BAR);}
		
		if(config.getProperty("marimba.interactiveconsole.completion.timer") != null){
			TIMER_COMPLETION = Integer.parseInt(config.getProperty("marimba.interactiveconsole.completion.timer"));
			if (DEBUG) System.out.println(DebugInfo() + "Initialized TIMER_COMPLETION : " + TIMER_COMPLETION);}	
		
		
		/*************************************Resetting Reboot Message Variables*************************************/
		
		REBOOTMESSAGE_COMPLETE = APPLICATION_NAME + " " + APPLICATION_VERSION + " Update is complete. A reboot is required to complete the installation.";
		REBOOTMESSAGE_RESTARTREQUIRED = "Please do not attempt to access " + APPLICATION_NAME + " before restarting the computer.";
		REBOOTMESSAGE_SAVEWORK = "\n\n***Please save all your work. This computer will restart in 5 minutes***";
		
		if(config.getProperty("marimba.interactiveconsole.rebootmessage.updatecomplete") != null){
			REBOOTMESSAGE_COMPLETE = config.getProperty("marimba.interactiveconsole.rebootmessage.updatecomplete");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized REBOOTMESSAGE_COMPLETE : " + REBOOTMESSAGE_COMPLETE);}
		
		if(config.getProperty("marimba.interactiveconsole.rebootmessage.restarrequired") != null){
			REBOOTMESSAGE_RESTARTREQUIRED = config.getProperty("marimba.interactiveconsole.rebootmessage.restarrequired");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized REBOOTMESSAGE_RESTARTREQUIRED : " + REBOOTMESSAGE_RESTARTREQUIRED);}
		
		if(config.getProperty("marimba.interactiveconsole.rebootmessage.savework") != null){
			REBOOTMESSAGE_SAVEWORK = config.getProperty("marimba.interactiveconsole.rebootmessage.savework");
			if (DEBUG) System.out.println(DebugInfo() + "Initialized REBOOTMESSAGE_SAVEWORK : " + REBOOTMESSAGE_SAVEWORK);}	
		
		if(config.getProperty("marimba.interactiveconsole.reboot.timer") != null){
			TIMER_REBOOT = Integer.parseInt(config.getProperty("marimba.interactiveconsole.reboot.timer"));
			if (DEBUG) System.out.println(DebugInfo() + "Initialized TIMER_REBOOT : " + TIMER_REBOOT);}		
		
		/*************************************Initializing/Resetting InteractiveConsole Variables*************************************/
		
		//Initializing marimba.interactiveconsole.proceed.enabled
		if(getParameter("marimba.interactiveconsole.proceed.enabled") == null)
		{		
			setParameter("marimba.interactiveconsole.proceed.enabled", "true");	
			//Resetting marimba.interactiveconsole.proceed.enabled
			if(config.getProperty("marimba.interactiveconsole.proceed.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.proceed.enabled' with Package configured value");
				setParameter("marimba.interactiveconsole.proceed.enabled", config.getProperty("marimba.interactiveconsole.proceed.enabled"));
			}
		}
		
		//Initializing marimba.interactiveconsole.postpone.enabled
		if(getParameter("marimba.interactiveconsole.postpone.enabled") == null)
		{	
			setParameter("marimba.interactiveconsole.postpone.enabled", "true");
			//Resetting marimba.interactiveconsole.postpone.enabled
			if(config.getProperty("marimba.interactiveconsole.postpone.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.postpone.enabled' with Package configured value");
				setParameter("marimba.interactiveconsole.postpone.enabled", config.getProperty("marimba.interactiveconsole.postpone.enabled"));
			}
		}
		
		//Initializing marimba.interactiveconsole.snooze.enabled
		if(getParameter("marimba.interactiveconsole.snooze.enabled") == null)
		{			
			setParameter("marimba.interactiveconsole.snooze.enabled", "true");
			//Resetting marimba.interactiveconsole.snooze.enabled
			if(config.getProperty("marimba.interactiveconsole.snooze.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.snooze.enabled' with Package configured value");
				setParameter("marimba.interactiveconsole.snooze.enabled", config.getProperty("marimba.interactiveconsole.snooze.enabled"));
			}
		}
		
		//Initializing marimba.interactiveconsole.progress.enabled
		if(getParameter("marimba.interactiveconsole.progress.enabled") == null)
		{			
			setParameter("marimba.interactiveconsole.progress.enabled", "true");
			//Resetting marimba.interactiveconsole.progress.enabled
			if(config.getProperty("marimba.interactiveconsole.progress.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.progress.enabled' with Package configured value");
				setParameter("marimba.interactiveconsole.progress.enabled", config.getProperty("marimba.interactiveconsole.progress.enabled"));
			}
		}
		
		//Initializing marimba.interactiveconsole.terminateprocess.enabled
		if(getParameter("marimba.interactiveconsole.terminateprocess.enabled") == null)
		{			
			setParameter("marimba.interactiveconsole.terminateprocess.enabled", "false");
			//Resetting marimba.interactiveconsole.terminateprocess.enabled
			if(config.getProperty("marimba.interactiveconsole.terminateprocess.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.terminateprocess.enabled' with Package configured value");
				setParameter("marimba.interactiveconsole.terminateprocess.enabled", config.getProperty("marimba.interactiveconsole.terminateprocess.enabled"));
				//Initializing marimba.interactiveconsole.terminateprocess.list
				if(getParameter("marimba.interactiveconsole.terminateprocess.list") == null)
				{
					if(config.getProperty("marimba.interactiveconsole.terminateprocess.list") != null)
					{
						setParameter("marimba.interactiveconsole.terminateprocess.list", config.getProperty("marimba.interactiveconsole.terminateprocess.list"));
						if (DEBUG) System.out.println(DebugInfo() + "Initialized Process Termination List : " + getParameter("marimba.interactiveconsole.terminateprocess.list"));
					}
				}
			}
		}						

		//Initializing marimba.interactiveconsole.postpone.maxDays
		if(getParameter("marimba.interactiveconsole.postpone.maxDays")==null)
		{		
			setParameter("marimba.interactiveconsole.postpone.maxDays", "30");
			//Resetting marimba.interactiveconsole.postpone.maxDays
			if(config.getProperty("marimba.interactiveconsole.postpone.maxDays") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.postpone.maxDays' with Package configured value");
				setParameter("marimba.interactiveconsole.postpone.maxDays", config.getProperty("marimba.interactiveconsole.postpone.maxDays"));
			}
		}
		
		//Initializing marimba.interactiveconsole.postpone.maxAttempts
		if(getParameter("marimba.interactiveconsole.postpone.maxAttempts")==null)
		{	
			setParameter("marimba.interactiveconsole.postpone.maxAttempts", "10");
			//Resetting marimba.interactiveconsole.postpone.maxAttempts
			if(config.getProperty("marimba.interactiveconsole.postpone.maxAttempts") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.postpone.maxAttempts' with Package configured value");
				setParameter("marimba.interactiveconsole.postpone.maxAttempts", config.getProperty("marimba.interactiveconsole.postpone.maxAttempts"));
			}
		}
		
		//Initializing marimba.interactiveconsole.forceinstall.enabled
		if(getParameter("marimba.interactiveconsole.forceinstall.enabled")==null)
		{	
			setParameter("marimba.interactiveconsole.forceinstall.enabled", "false");
			//Resetting marimba.interactiveconsole.forceinstall.enabled
			if(config.getProperty("marimba.interactiveconsole.forceinstall.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.forceinstall.enabled' with Package configured value");
				setParameter("marimba.interactiveconsole.forceinstall.enabled", config.getProperty("marimba.interactiveconsole.forceinstall.enabled"));
			}
		}

		//Initializing marimba.interactiveconsole.channelmanager.enabled
		if(getParameter("marimba.interactiveconsole.channelmanager.enabled")==null)
		{	
			setParameter("marimba.interactiveconsole.channelmanager.enabled", "false");
			//Resetting marimba.interactiveconsole.channelmanager.enabled
			if(config.getProperty("marimba.interactiveconsole.channelmanager.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.channelmanager.enabled' with Package configured value");
				setParameter("marimba.interactiveconsole.channelmanager.enabled", config.getProperty("marimba.interactiveconsole.channelmanager.enabled"));
				
				//Initializing marimba.interactiveconsole.channelmanager.urls
				if(getParameter("marimba.interactiveconsole.channelmanager.urls")==null)
				{
					if(config.getProperty("marimba.interactiveconsole.channelmanager.urls") != null)
					{
    					setParameter("marimba.interactiveconsole.channelmanager.urls", config.getProperty("marimba.interactiveconsole.channelmanager.urls"));
        				if (DEBUG) System.out.println(DebugInfo() + "Initialized Channel Manager Urls : " + getParameter("marimba.interactiveconsole.channelmanager.urls"));
        			}
				}
						
				//Initializing marimba.interactiveconsole.channelmanager.args
				if(getParameter("marimba.interactiveconsole.channelmanager.args")==null)
				{
					if(config.getProperty("marimba.interactiveconsole.channelmanager.args") != null)
					{
						setParameter("marimba.interactiveconsole.channelmanager.args", config.getProperty("marimba.interactiveconsole.channelmanager.args"));
						if (DEBUG) System.out.println(DebugInfo() + "Initialized Channel Manager Arguments : " + getParameter("marimba.interactiveconsole.channelmanager.args"));
					}
				}
					
				//Initializing marimba.interactiveconsole.channelmanager.timeout
				if(getParameter("marimba.interactiveconsole.channelmanager.timeout")==null)
				{
					if(config.getProperty("marimba.interactiveconsole.channelmanager.timeout") != null)
					{
						setParameter("marimba.interactiveconsole.channelmanager.timeout", config.getProperty("marimba.interactiveconsole.channelmanager.timeout"));
						if (DEBUG) System.out.println(DebugInfo() + "Initialized Channel Manager Timeout : " + getParameter("marimba.interactiveconsole.channelmanager.timeout"));
					}
				}
			}
		}
		
		//Initializing marimba.interactiveconsole.reboot.abort.enabled
		if(getParameter("marimba.interactiveconsole.reboot.abort.enabled")==null)
		{	
			setParameter("marimba.interactiveconsole.reboot.abort.enabled", "false");
			//Resetting marimba.interactiveconsole.reboot.abort.enabled
			if(config.getProperty("marimba.interactiveconsole.reboot.abort.enabled") != null)
			{
				System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.reboot.abort.enabled' with Package configured value");
				setParameter("marimba.interactiveconsole.reboot.abort.enabled", config.getProperty("marimba.interactiveconsole.reboot.abort.enabled"));
			}
		}
		
		/*************************************Initializing default InteractiveConsole Variables*************************************/
		
		//ExitCode
		if(getParameter("marimba.interactiveconsole.exitcode")==null)		
			setParameter("marimba.interactiveconsole.exitcode", "1");

		//Select Snooze Number
		if(getParameter("marimba.interactiveconsole.snooze.index")==null)			
			setParameter("marimba.interactiveconsole.snooze.index", "1");
		
		//Select Snooze Hours
		if(getParameter("marimba.interactiveconsole.snooze.choosen")==null)			
			setParameter("marimba.interactiveconsole.snooze.choosen", "0");
		
		//Last Prompted
		if(getParameter("marimba.interactiveconsole.prompt.last")==null)			
			setParameter("marimba.interactiveconsole.prompt.last", "0");
		
		//Next Scheduled Prompt(If Postponed)
		if(getParameter("marimba.interactiveconsole.prompt.next")==null)			
			setParameter("marimba.interactiveconsole.prompt.next", "0");
		
		//Total Prompt Times
		if(getParameter("marimba.interactiveconsole.prompt.count")==null)			
			setParameter("marimba.interactiveconsole.prompt.count", "0");
		
		//Which Button Clicked(Proceed/Postpone/Snooze)
		if(getParameter("marimba.interactiveconsole.button.clicked")==null)			
			setParameter("marimba.interactiveconsole.button.clicked", "none");						
		
		//Is Reboot Really Required?
		if(getParameter("marimba.interactiveconsole.reboot.required")==null)			
			setParameter("marimba.interactiveconsole.reboot.required", "false");
		
		//Mandatory Install Date(Days)
		if(getParameter("marimba.interactiveconsole.forceinstall.active")==null)
			setParameter("marimba.interactiveconsole.forceinstall.active", "0");
						
		if(getParameter("marimba.interactiveconsole.forceinstall.enabled").equals("true"))	
		{
			System.out.println(DebugInfo() + "Reconfiguring 'marimba.interactiveconsole.forceinstall.active' with Package configured value");
			setParameter("marimba.interactiveconsole.forceinstall.active", Long.toString(CURRENT_EPOCH + (86400*(Integer.parseInt(getParameter("marimba.interactiveconsole.postpone.maxDays"))))));
			ForceInstallOn = new java.text.SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z ").format(new Date (Long.valueOf(getParameter("marimba.interactiveconsole.forceinstall.active"))*1000));
		}
		
		if (DEBUG) System.out.println(DebugInfo() + "Current TimeStamp: " + CURRENT_EPOCH);
		System.out.println(DebugInfo() + "Current Display Date: " + new java.text.SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z ").format(new Date (CURRENT_EPOCH*1000)));				
		
		//Display next prompt related information only if they are already set
		if(!(Long.valueOf(getParameter("marimba.interactiveconsole.prompt.next")) == 0))
		{
			if (DEBUG) System.out.println(DebugInfo() + "Next Prompt TimeStamp: " + Long.valueOf(getParameter("marimba.interactiveconsole.prompt.next")));
			System.out.println(DebugInfo() + "Next Prompt Display Date: " + new java.text.SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z ").format(new Date (Long.valueOf(getParameter("marimba.interactiveconsole.prompt.next"))*1000)));
			if (DEBUG) System.out.println(DebugInfo() + "Difference: " + (CURRENT_EPOCH - Long.valueOf(getParameter("marimba.interactiveconsole.prompt.next")))/60 + " Minutes");
		}	
	}	
	
	protected void channelUsage()
    {
        System.out.println(DebugInfo() + "Incorrects Arguments Passed. Syntax--");
        System.out.println(DebugInfo() + "Runchannel " + ctx.getChannelURL() + " -Prompt/-Completed/-Reboot");
    }
	
	protected static int shutdowntool(int timer) 
	{
		if (DEBUG) System.out.println(DebugInfo() + "REBOOTMESSAGE_COMPLETE : " + REBOOTMESSAGE_COMPLETE);
		if (DEBUG) System.out.println(DebugInfo() + "REBOOTMESSAGE_RESTARTREQUIRED : " + REBOOTMESSAGE_RESTARTREQUIRED);
		if (DEBUG) System.out.println(DebugInfo() + "REBOOTMESSAGE_SAVEWORK : " + REBOOTMESSAGE_SAVEWORK);
		
		String rebootmessage = REBOOTMESSAGE_COMPLETE + REBOOTMESSAGE_RESTARTREQUIRED + REBOOTMESSAGE_SAVEWORK;
		
		String[] command;	
		
		if (getParameter("marimba.interactiveconsole.reboot.abort.enabled").equals("false")) 
		{
			command = new String[] {CHANNEL_DIRECTORY + "\\data\\scripts\\" + "ShutdownTool.exe" , "/r", "/t:"+ timer*60, "/m:0", "/d:" + rebootmessage, "/c", "/n"}; //No Option To Abort
		}
		else
		{
			command = new String[] {CHANNEL_DIRECTORY + "\\data\\scripts\\" + "ShutdownTool.exe" , "/r", "/t:"+ timer*60, "/m:0", "/d:" + rebootmessage, "/n"};	//Displays Option To Abort
		}		
				
		if(DEBUG) 
		{	
			System.out.print(DebugInfo() + "Formulated Command: ");	
			for (String element : command)	{System.out.print(element + " ");}	System.out.println();	
		}
		try 
		{
			ProcessBuilder pb = new ProcessBuilder(command);
			Process process = pb.start();
			if(DEBUG) System.out.println(DebugInfo() + "Initiating Process: " + pb.command());		
			return 0;						
		} 
		catch (IOException e) {	e.printStackTrace(); return 1;	}
	}
	
	//PROMPT
	public MarimbaInteractiveConsole(String frameTitle, String AppName, String AppVersion, boolean Mandatory,boolean proceed,boolean postpone) 
	{		
		getContentPane().setBackground(new Color(0, 0, 0, 0));
		getContentPane().setLayout(null);
		setTitle(frameTitle);
		setType(Type.UTILITY);
		setResizable(false);
		setAlwaysOnTop(true);
		setBounds(100, 100, 800, 558);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JLabel lblApplicationName = new JLabel("Mandatory Update Required - " + AppName + " " + AppVersion);
		lblApplicationName.setForeground(Color.RED);
		lblApplicationName.setBounds(0, 150, 800, 50);
		lblApplicationName.setHorizontalAlignment(SwingConstants.CENTER);
		lblApplicationName.setFont(new Font("Copperplate Gothic Light", Font.BOLD, 21));
		getContentPane().add(lblApplicationName);
		
		JLabel lblTimetaken = new JLabel(convertToMultiline(PROMPTMESSAGE_TIMETAKEN));
		lblTimetaken.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTimetaken.setHorizontalAlignment(SwingConstants.CENTER);
		lblTimetaken.setBounds(50, 350, 700, 50);
		lblTimetaken.setForeground(Color.BLACK);
		getContentPane().add(lblTimetaken);
		
		JLabel lblRestart = new JLabel(convertToMultiline(PROMPTMESSAGE_RESTART));
		lblRestart.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblRestart.setHorizontalAlignment(SwingConstants.CENTER);
		lblRestart.setBounds(50, 400, 700, 50);
		lblRestart.setForeground(Color.BLACK);
		getContentPane().add(lblRestart);
		
		JLabel labelforceStart = new JLabel("AFTER " + ForceInstallOn + "YOU WILL NOT LONGER BE PROMPTED & INSTALLATION WILL BEGIN"); 
		labelforceStart.setHorizontalAlignment(SwingConstants.CENTER);
		labelforceStart.setForeground(Color.RED);
		labelforceStart.setFont(new Font("Tahoma", Font.BOLD, 13));
		labelforceStart.setBounds(50, 430, 700, 50);
		//Show only if force install is enabled
		if(Mandatory)getContentPane().add(labelforceStart);
		
		BUTTON_PROCEED = new JButton("PROCEED");
		BUTTON_PROCEED.setBackground(new Color(143, 188, 143));
		BUTTON_PROCEED.setFont(new Font("Tahoma", Font.BOLD, 11));
		BUTTON_PROCEED.setBounds(200, 480, 100, 50);
		BUTTON_PROCEED.addActionListener(this);
		//To Enable/Disable Proceed option
		BUTTON_PROCEED.setEnabled(proceed);
		getContentPane().add(BUTTON_PROCEED);
		
		BUTTON_POSTPONE = new JButton("POSTPONE");
		BUTTON_POSTPONE.setBackground(new Color(143, 188, 143));
		BUTTON_POSTPONE.setFont(new Font("Tahoma", Font.BOLD, 11));
		BUTTON_POSTPONE.setBounds(500, 480, 100, 50);
		BUTTON_POSTPONE.addActionListener(this);
		//To Enable/Disable Postpone option
		BUTTON_POSTPONE.setEnabled(postpone);
		getContentPane().add(BUTTON_POSTPONE);
		
		PROMPTMESSAGE_BACKGROUNDLABEL = new JLabel("");
		PROMPTMESSAGE_BACKGROUNDLABEL.setEnabled(true);
		PROMPTMESSAGE_BACKGROUNDLABEL.setIcon(new ImageIcon(this.getClass().getResource(background)));
		PROMPTMESSAGE_BACKGROUNDLABEL.setBounds(0, 0, 800, 500);
		getContentPane().add(PROMPTMESSAGE_BACKGROUNDLABEL);		
	}
	
	//PROGRESS
	public MarimbaInteractiveConsole(String frameTitle, String headermessage, String barmessage, String footermessage)
	{

		getContentPane().setBackground(new Color(255, 255, 224));
		getContentPane().setLayout(null);
		setType(Type.UTILITY);
		setResizable(false);
		setAlwaysOnTop(true);
		setVisible(false);
		validate();
		setBounds(100, 100, 845, 85);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle(frameTitle);		
		
		jtext01 = new JTextField();
		jtext01.setBounds(140, 0, 560, 20);
		jtext01.setForeground(Color.DARK_GRAY);
		jtext01.setBackground(Color.WHITE);
		jtext01.setHorizontalAlignment(SwingConstants.CENTER);
		jtext01.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 12));
		jtext01.setEditable(false);
		jtext01.setText(headermessage);
		getContentPane().add(jtext01);		
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(140, 20, 560, 20);
		progressBar.setIndeterminate(true);
		progressBar.setForeground(new Color(0, 128, 128));
		progressBar.setBackground(Color.WHITE);
		progressBar.setFont(new Font("Tahoma", Font.BOLD, 11));
		progressBar.setString(barmessage);
		getContentPane().add(progressBar);
				
		jtext02 = new JTextField();
		jtext02.setBounds(140, 40, 560, 20);
		jtext02.setForeground(Color.DARK_GRAY);
		jtext02.setBackground(Color.WHITE);
		jtext02.setFont(new Font("Tahoma", Font.BOLD, 11));
		jtext02.setHorizontalAlignment(SwingConstants.CENTER);
		jtext02.setEditable(false);
		jtext02.setText(footermessage);
		getContentPane().add(jtext02);
				
		labelleft = new JLabel("");
		labelleft.setIcon(new ImageIcon(this.getClass().getResource(logo)));
		labelleft.setBounds(0, 0, 145, 65);
		getContentPane().add(labelleft);		
		
		labelright = new JLabel("");
		labelright.setIcon(new ImageIcon(this.getClass().getResource(logo)));
		labelright.setBounds(700, 0, 145, 65);
		getContentPane().add(labelright);
	}

	//SNOOZE
	public MarimbaInteractiveConsole(String frameTitle, String AppName, String AppVersion, boolean snooze, int snoozeLeft) 
	{
		getContentPane().setBackground(new Color(0, 0, 0, 0));
		getContentPane().setLayout(null);
		setTitle(frameTitle);
		setType(Type.UTILITY);
		setResizable(false);
		setAlwaysOnTop(true);
		setBounds(100, 100, 400, 150);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JLabel lblMandatoryUpdateRequired = new JLabel("Mandatory Update Required - " + AppName + " " + AppVersion);
		lblMandatoryUpdateRequired.setHorizontalAlignment(SwingConstants.CENTER);
		lblMandatoryUpdateRequired.setForeground(Color.BLACK);
		lblMandatoryUpdateRequired.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblMandatoryUpdateRequired.setBounds(0, 0, 394, 50);
		getContentPane().add(lblMandatoryUpdateRequired);
		
		SNOOZE_LIST.setModel(new DefaultComboBoxModel(new String[] {"1 Hour", "2 Hours", "4 Hours", "6 Hours", "8 Hours"}));
		SNOOZE_LIST.setFont(new Font("Tahoma", Font.BOLD, 11));
		SNOOZE_LIST.setBackground(Color.LIGHT_GRAY);
		SNOOZE_LIST.setMaximumRowCount(5);
		SNOOZE_LIST.setBounds(214, 90, 75, 25);
		SNOOZE_LIST.addItemListener(this);
		SNOOZE_LIST.setSelectedItem(-1);
		getContentPane().add(SNOOZE_LIST);
		
		BUTTON_SNOOZE = new JButton("SNOOZE");
		BUTTON_SNOOZE.setFont(new Font("Tahoma", Font.BOLD, 11));
		BUTTON_SNOOZE.setBackground(Color.LIGHT_GRAY);
		BUTTON_SNOOZE.setBounds(299, 90, 85, 25);
		BUTTON_SNOOZE.addActionListener(this);
		BUTTON_SNOOZE.setEnabled(snooze);	//To Enable/Disable Postpone option
		getContentPane().add(BUTTON_SNOOZE);
		
		JLabel lblSnoozeIn = new JLabel("Click Snooze to be reminded in....");
		lblSnoozeIn.setHorizontalAlignment(SwingConstants.CENTER);
		lblSnoozeIn.setForeground(Color.BLACK);
		lblSnoozeIn.setBounds(0, 87, 215, 30);
		getContentPane().add(lblSnoozeIn);
		
		JLabel lblSnoozeLeft = new JLabel("You may snooze this upto " + snoozeLeft + " times");
		lblSnoozeLeft.setHorizontalAlignment(SwingConstants.CENTER);
		lblSnoozeLeft.setForeground(Color.BLACK);
		lblSnoozeLeft.setBounds(44, 50, 302, 25);
		getContentPane().add(lblSnoozeLeft);	
	}
	
	//COMPLETED
	public MarimbaInteractiveConsole(String frameTitle, String AppName, String AppVersion, int min) 
	{
		setType(Type.UTILITY);
		setResizable(false);
		getContentPane().setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
		getContentPane().setForeground(Color.WHITE);
		getContentPane().setLayout(null);
		setAlwaysOnTop(true);
		setBounds(100, 100, 845, 85);
		setLocationRelativeTo(null);
		setTitle(frameTitle);		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);			
		
		jtext01 = new JTextField();
		jtext01.setBounds(140, 0, 560, 20);
		jtext01.setForeground(Color.BLACK);
		jtext01.setBackground(Color.WHITE);
		jtext01.setHorizontalAlignment(SwingConstants.CENTER);
		jtext01.setFont(new Font("Copperplate Gothic Light", Font.BOLD, 12));
		jtext01.setEditable(false);
		jtext01.setText(COMPLETEDMESSAGE_HEADER);
		getContentPane().add(jtext01);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(140, 20, 560, 20);
		progressBar.setForeground(new Color(0, 128, 128));
		progressBar.setBackground(Color.WHITE);
		progressBar.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
		progressBar.setString(COMPLETEDMESSAGE_BAR);
		getContentPane().add(progressBar);
		
		jtext02 = new JTextField();
		jtext02.setBounds(140, 40, 560, 20);
		jtext02.setForeground(Color.BLACK);
		jtext02.setBackground(Color.WHITE);
		jtext02.setFont(new Font("Copperplate Gothic Light", Font.BOLD, 12));
		jtext02.setHorizontalAlignment(SwingConstants.CENTER);
		jtext02.setEditable(false);
		jtext02.addActionListener(this);
		getContentPane().add(jtext02);		
		
		labelleft = new JLabel("");
		labelleft.setBounds(0, 0, 145, 65);
		labelleft.setIcon(new ImageIcon(this.getClass().getResource(logo)));
		getContentPane().add(labelleft);
		
		labelright = new JLabel("");
		labelright.setBounds(700, 0, 145, 65);
		labelright.setIcon(new ImageIcon(this.getClass().getResource(logo)));
		getContentPane().add(labelright);				
	}
	
	public void itemStateChanged(ItemEvent p) 
	{
		if(p.getStateChange() == ItemEvent.SELECTED) 
		{	
			SNOOZE_INDEX = SNOOZE_LIST.getSelectedIndex();	
			if (DEBUG) System.out.println(DebugInfo() + "Index Selected per Item Listener: " + SNOOZE_INDEX);
		}
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == BUTTON_PROCEED)
		{
			if (DEBUG) System.out.println(DebugInfo() + "Proceed Button was clicked. Updating Boolean userSelectionDone with " + userSelectionDone);
			System.out.println(DebugInfo() + "User Choose to Proceed, So We Will Proceed");
			setParameter("marimba.interactiveconsole.exitcode", "0");
			setParameter("marimba.interactiveconsole.button.clicked", "proceed");
			setParameter("marimba.interactiveconsole.reboot.required", "true");
			System.out.println(DebugInfo() + "Disposing Proceed/Postpone Frame");	closeframe();			
			if(getParameter("marimba.interactiveconsole.progress.enabled").equals("true"))	showProgressMsg();
			userSelectionDone = true;			
		}
		
		else if(e.getSource() == BUTTON_POSTPONE)
		{
			System.out.println(DebugInfo() + "User Choose to Postpone, So We Will Postpone");
			setParameter("marimba.interactiveconsole.exitcode", "1");
			setParameter("marimba.interactiveconsole.reboot.required", "false");			
			System.out.println(DebugInfo() + "Disposing Proceed/Postpone Frame");	closeframe();
			setParameter("marimba.interactiveconsole.button.clicked", "postpone");	ifpostponed();
			setParameter("marimba.interactiveconsole.postpone.maxAttempts", Integer.toString(Integer.parseInt(getParameter("marimba.interactiveconsole.postpone.maxAttempts"))-1));
			if(!(Integer.parseInt(getParameter("marimba.interactiveconsole.postpone.maxAttempts")) > 0))	setParameter("marimba.interactiveconsole.postpone.enabled", "false");			
			System.out.println(DebugInfo() + "Waiting On User's Choice.");
		} 
		
		else if(e.getSource() == BUTTON_SNOOZE)	
		{	
			if (DEBUG) System.out.println(DebugInfo() + "Snooze Button was clicked. Updating Boolean userSelectionDone with " + userSelectionDone);
			if(!(Integer.parseInt(getParameter("marimba.interactiveconsole.postpone.maxAttempts")) > 0))	setParameter("marimba.interactiveconsole.postpone.enabled", "false");
			System.out.println(DebugInfo() + "Index Selected per Action Listener: " + SNOOZE_INDEX);
			setParameter("marimba.interactiveconsole.snooze.index", Integer.toString(SNOOZE_INDEX));
			System.out.println(DebugInfo() + "User Choose to Postpone: " + SNOOZE_LIST.getSelectedItem());
			setParameter("marimba.interactiveconsole.snooze.choosen", SNOOZE_LIST.getSelectedItem().toString());
			setParameter("marimba.interactiveconsole.prompt.next", Long.toString(CURRENT_EPOCH + (3600*(Integer.parseInt(SNOOZE_LIST.getSelectedItem().toString().substring(0 ,SNOOZE_LIST.getSelectedItem().toString().indexOf(" ")))))));
			setParameter("marimba.interactiveconsole.exitcode", "1");
			setParameter("marimba.interactiveconsole.button.clicked", "snooze");
			setParameter("marimba.interactiveconsole.reboot.required", "false");
			if (DEBUG) System.out.println(DebugInfo() + "Disposing Snooze Frame");	closeframe();
			userSelectionDone = true;
		}		
		else if(e.getSource() == TIMER)
		{
			jtext02.setText("This Notification Message Will Close in: " + TIMER_COMPLETION + " Minutes "+ String.format("%02d", seconds)+ " Seconds");
			seconds--;	//Decrement Seconds
			//If Countdown Expired
			if(seconds==0 && TIMER_COMPLETION==0)
			{
				TIMER.stop();
				System.out.println(DebugInfo() + "Countdown Expired. Disposing Completion Dialog");
				TIMER_EXPIRED = true;
				closeframe();				
			}
			//Reset Seconds to decrement from 59.
			if(seconds==0 || seconds<0)
			{
				seconds=59; 
				TIMER_COMPLETION--;
				System.out.println(DebugInfo() + "Seconds Reset & Minute Decrement Completed");				
			}			
		}				
	}
	
	protected void closeframe()
	{
		this.setVisible(false);	//Notify JFrame to Close	
		this.dispose();			//Kills JFrame	
	}
	
	protected void startFrame()
	{
		this.setVisible(true);	//Start JFrame 
	}
	
	protected int MarimbaChannelManager()
	{		
		System.out.println(DebugInfo() + "Channel running in " + phase + " phase");
		
		LIST_CHANNELURLS = new ArrayList(Arrays.asList(getParameter("marimba.interactiveconsole.channelmanager.urls").split("\\s*,\\s*")));
	    LIST_CHANNELARGS = new ArrayList(Arrays.asList(getParameter("marimba.interactiveconsole.channelmanager.args").split("\\s*,\\s*")));
	    CHANNEL_TIMEOUT = getParameter("marimba.interactiveconsole.channelmanager.timeout");
	    
		System.out.println(DebugInfo() + "Retrieved channel list from parameter 'channelurls' : " + LIST_CHANNELURLS);
		System.out.println(DebugInfo() + "Retrieved channel argument list from parameter 'channelargs' : " + LIST_CHANNELARGS);
		System.out.println(DebugInfo() + "Individual channel operation can only wait a maximum of " + CHANNEL_TIMEOUT + " minutes");
		
		if (CHANNEL_TIMEOUT != null) {	to = Integer.parseInt(CHANNEL_TIMEOUT) * 60 * 1000;	}
		
		System.out.println(DebugInfo() + "Setting up individual ArrayList for channel urls & its arguments");	
		System.out.println(DebugInfo() + "Successfully manipulated Channel urls & its Arguments Array List for further processing.......");
    	System.out.println(DebugInfo() + "The Operation Matrix:");
	    
	    for (int i = 0; i<LIST_CHANNELURLS.size(); i++) 
	    {  	  
	    	String channeltoprocess = null;
	    	String argument = null;
	    	for (int j = 0; j<LIST_CHANNELARGS.size(); j++) 
	    	{		
	    		if (i == j)
	    		{ 
	    			channeltoprocess = LIST_CHANNELURLS.get(i);
	    			argument = LIST_CHANNELARGS.get(j);
	    		}
	    	}
	    	System.out.println(DebugInfo() + "\t" + i + ". Channel: " + channeltoprocess + " will be process " + argument + " argument");
	    }
	    System.out.println(DebugInfo() + "Now we will start processing them one at a time........ ");
	    
		try
		{
			for (int i = 0; i<LIST_CHANNELURLS.size(); i++) 
		    {  	  
				IChannel channeltouninstall = null;
				IChannel channeltoinstall = null;
				
				for (int j = 0; j<LIST_CHANNELARGS.size(); j++) 
		    	{		
		    		if (i == j)
		    		{ 
		    			String channeltoprocess = LIST_CHANNELURLS.get(i);
		    			String argument = LIST_CHANNELARGS.get(j);
		    			String[] argv = null;
		    			if (argument != null) {
		    				StringTokenizer tok = new StringTokenizer(argument, " ");
		    				argv = new String[tok.countTokens()];
		    				for (int k = 0; tok.hasMoreTokens(); k++) {
		    				argv[k] = tok.nextToken();
		    				}
		    			}
		    			System.out.println(DebugInfo() + "Initiating operation " + Arrays.toString(argv) + " On "+ channeltoprocess);
		    			IChannel url = workspace.getChannel(channeltoprocess);//for validation
		    			System.out.println(DebugInfo() + "Verifying tuner workspace for the presence of " + channeltoprocess);
		    			
		    			if (argument!=null && argument.equals(ACTION_REMOVE))
		    			{
		    				//All UnInstall Actions to be handled here
		    				channeltouninstall = workspace.getChannel(channeltoprocess);
		    				if (url!= null)
							{
								System.out.println(DebugInfo() + "IChannel " + channeltoprocess + " found in Workspace, So will process " + Arrays.toString(argv) + " to it now......");
								//Launching Channel with specified arguments
								IActive active = launcher.launch(channeltoprocess, argv , true, null);
								
								if (waitFor(active, to)) 
								{
									System.out.println(DebugInfo() + "Operation " + Arrays.toString(argv) + " On "+ channeltoprocess + " completed successfully");
									//Removing Channel from Workspace
									launcher.remove(channeltoprocess);
									while (channeltouninstall.getChannelStatus() == IChannel.CH_REMOVED) 
									{
										System.out.println(DebugInfo() + "Channel " + channeltoprocess + " removed from Workspace");
									}									
								}
								else 
								{
									System.out.println(DebugInfo() + "Timed out running " + channeltouninstall);
									return 2;
								}
							}			    				
		    				else
							{
								System.out.println(DebugInfo() + "Channel " + channeltoprocess + " doesn't exist in Workspace, moving on......");								
							}			    				
		    			}
		    			//End of UnInstall Actions Loop
		    			
		    			else if (argument!=null && argument.equals(ACTION_INSTALL))
		    			{
		    				if (url!=null)
		    				{
		    					channeltoinstall = workspace.getChannel(channeltoprocess);
		    					if(getAdapterState(channeltoinstall)!=null && getAdapterState(channeltoinstall).equals(CHANNEL_STATUS))
		    					{
		    						System.out.println(DebugInfo() + "Channel " + channeltoprocess + " already installed, moving on......");
			    				}
		    					else
		    					{
		    						System.out.println(DebugInfo() + "Channel " + channeltoprocess + " found in Workspace but not installed, So will process " + Arrays.toString(argv) + " to it now......");
			    					IActive active = launcher.launch(channeltoprocess, argv, false, null);
									if (waitFor(active, to)) 
									{	
										channeltoinstall = workspace.getChannel(channeltoprocess);
										if (channeltoinstall!= null)
										{	
											if (getAdapterState(channeltoinstall)!=null && getAdapterState(channeltoinstall).equals(CHANNEL_STATUS))
											{
												System.out.println(DebugInfo() + "Channel Adapter State: " + getAdapterState(channeltoinstall));
											}
											System.out.println(DebugInfo() + "Operation " + Arrays.toString(argv) + " On "+ channeltoprocess + " Completed Successfully");
										}
									}
									else 
									{
										System.out.println(DebugInfo() + "\t" + "Timed out running " + channeltoprocess);
										return 2;
									}
			    				}
		    				}
		    				else
		    				{	
		    					IActive active = launcher.launch(channeltoprocess, argv, false, null);
								if (waitFor(active, to)) 
								{	
									channeltoinstall = workspace.getChannel(channeltoprocess);
									if (channeltoinstall!= null)
									{	
										if (getAdapterState(channeltoinstall)!=null && getAdapterState(channeltoinstall).equals(CHANNEL_STATUS))
										{
											System.out.println(DebugInfo() + "\t" + "Channel Adapter State: " + getAdapterState(channeltoinstall));
										}
										System.out.println(DebugInfo() + "\t" + "Operation " + Arrays.toString(argv) + " On "+ channeltoprocess + " Completed Successfully");
										System.out.println(DebugInfo() + "");
									}
								}
								else 
								{
									System.out.println(DebugInfo() + "\t" + "timed out running " + channeltoprocess);
									return 2;
								}
		    				}
		    			}
		    			//End of Install Action Loop
		    			
		    			else 
		    			{
		    				System.out.println(DebugInfo() + "Unable to enumerate channel action from property: channelargs, failing the channel... ");
							return 1;
		    			}
		    		}
		    	}
		    }
			System.out.println(DebugInfo() + "Channel Processing Completed. Begin Validation.......... ");
			
			Thread.sleep(10000);	//Pausing 10 Seconds for Channel Validation
			
			if(MarimbaChannelManagerValidation()) return 0; else return 1;		//Exiting Loop Successfully
		}
		catch (NumberFormatException | InterruptedException e) {e.printStackTrace();}
		return 1;	
	}
	
	protected boolean MarimbaChannelManagerValidation()
	{
		CHANNEL_VALIDATION = "passed";
		if(DEBUG) System.out.println(DebugInfo() + "Resetted 'validation' parameter with 'passed' for futher processing.");
		System.out.println(DebugInfo() + "Channel running in " + phase + " phase");
		
		LIST_CHANNELURLS = new ArrayList(Arrays.asList(getParameter("marimba.interactiveconsole.channelmanager.urls").split("\\s*,\\s*")));
	    LIST_CHANNELARGS = new ArrayList(Arrays.asList(getParameter("marimba.interactiveconsole.channelmanager.args").split("\\s*,\\s*")));
	    
		System.out.println(DebugInfo() + "Retrieved channel list from parameter 'channelurls' : " + LIST_CHANNELURLS);
		System.out.println(DebugInfo() + "Retrieved channel argument list from parameter 'channelargs' : " + LIST_CHANNELARGS);
		
		for (int i = 0; i<LIST_CHANNELURLS.size(); i++) 
	    {  	  
	    	String channeltoprocess = null;
	    	String argument = null;
	    	for (int j = 0; j<LIST_CHANNELARGS.size(); j++) 
	    	{		
	    		if (i == j)
	    		{ 
	    			channeltoprocess = LIST_CHANNELURLS.get(i);
	    			argument = LIST_CHANNELARGS.get(j);
	    			IChannel url = workspace.getChannel(channeltoprocess);
	    			
	    			if (argument!=null && argument.equals(ACTION_REMOVE))
	    			{
	    				if (url!= null) 
	    				{ 
	    					System.out.println(DebugInfo() + "Channel " + channeltoprocess + " ===> " + getAdapterState(url));	CHANNEL_VALIDATION = "failed";
	    				}
	    				else 
	    				{
	    					System.out.println(DebugInfo() + "Channel " + channeltoprocess + " ===> " + "removed (or) doesn't exist");
	    				}
	    			}
	    			
	    			else if (argument!=null && argument.equals(ACTION_INSTALL))
	    			{
	    				if (url!= null)
						{
							System.out.println(DebugInfo() + "Channel " + channeltoprocess + " ===> " + getAdapterState(url));
							if(!getAdapterState(url).equals(CHANNEL_STATUS)) { CHANNEL_VALIDATION = "failed"; }									
						}
	    				else 
	    				{ 
	    					System.out.println(DebugInfo() + "Channel " + channeltoprocess + " ===> " + "removed (or) doesn't exist");	CHANNEL_VALIDATION = "failed"; 
	    				}
	    			}	    			
	    		}
	    	}			    	
	    }
		
		System.out.println(DebugInfo() + "Overall Validation Status: " + CHANNEL_VALIDATION);
		if (CHANNEL_VALIDATION!=null && CHANNEL_VALIDATION.equals("failed")) return false;	else return true;
	}
	
	protected void ifpostponed()
	{
		try 
		{
			userSelectionDone = false;
			if (DEBUG) System.out.println(DebugInfo() + "userSelectionDone value before starting Snooze Frame" + userSelectionDone);
			if (DEBUG) System.out.println(DebugInfo() + "Starting Snooze Frame");
			if (DEBUG) System.out.println(DebugInfo() + "FrameTitle : " + FRAME_TITLE);
			if (DEBUG) System.out.println(DebugInfo() + "Application Name : " + APPLICATION_NAME);
			
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			
			MarimbaInteractiveConsole snoozeframe = new MarimbaInteractiveConsole(FRAME_TITLE, APPLICATION_NAME, APPLICATION_VERSION, Boolean.valueOf(getParameter("marimba.interactiveconsole.snooze.enabled")), Integer.parseInt(getParameter("marimba.interactiveconsole.postpone.maxAttempts")));
			snoozeframe.startFrame();
		} 
		catch (Exception e) {e.printStackTrace();}
	}
	
	protected void showProgressMsg()
	{
		try 
		{
			System.out.println(DebugInfo() + "Launching Progress Dialog");
			if (DEBUG) System.out.println(DebugInfo() + "FrameTitle : " + FRAME_TITLE);
			if (DEBUG) System.out.println(DebugInfo() + "PROGRESSMESSAGE_HEADER : " + PROGRESSMESSAGE_HEADER);
			if (DEBUG) System.out.println(DebugInfo() + "PROGRESSMESSAGE_BAR : " + PROGRESSMESSAGE_BAR);
			if (DEBUG) System.out.println(DebugInfo() + "PROGRESSMESSAGE_FOOTER : " + PROGRESSMESSAGE_FOOTER);
			
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			
			MarimbaInteractiveConsole progressframe = new MarimbaInteractiveConsole(FRAME_TITLE, PROGRESSMESSAGE_HEADER, PROGRESSMESSAGE_BAR, PROGRESSMESSAGE_FOOTER);
			progressframe.startFrame();			
		} 
		catch (Exception e) {e.printStackTrace();}
	}	
	
	//Lookup Channels
	protected static IChannel getIChannel(String channelname) 
	{
		String channelurl = (config != null) ? config.getProperty(channelname) : null;
		if(DEBUG) System.out.println(DebugInfo() + channelname + " retrived from property: " + channelurl);
		IChannel iChannelUrl = null;
		
		if (channelurl!= null)	
		{	
			iChannelUrl = workspace.getChannel(channelurl);	
			if(DEBUG) System.out.println(DebugInfo() + channelname +  " Found in Workspace" + ":" + iChannelUrl);
		}
		
		if (iChannelUrl == null) 
		{
			if(DEBUG) System.out.println(DebugInfo() + "Iterating the workspace to fetch " + channelname + " channel.....");
            IChannel[] channels = workspace.getChannels();
            for (int i = 0; i < channels.length; i++) 
        	{
        		if (channels[i].getPath().endsWith(channelname) || channels[i].getPath().toLowerCase().endsWith(channelname))	iChannelUrl = channels[i];
        	}
            if(DEBUG) System.out.println(DebugInfo() + channelname +  " Found in Workspace" + ":" + iChannelUrl);;
		}
		return iChannelUrl;		
	}
		
	protected static String getParameter(String propertyname)
	{
		String propertyvalue = null;
		if(DEBUG) System.out.println(DebugInfo() + "Searching for property: " + propertyname);
		propertyvalue = chConfig.getProperty(propertyname);
		if(DEBUG) System.out.println(DebugInfo() + "Retrieved value of " + propertyname + " is " + propertyvalue);
		return propertyvalue;
	}
	
	protected static void setParameter(String propertyname, String propertyvalue)
	{	
		boolean setvaluesuccess = false;
		chConfig.setProperty(propertyname, propertyvalue);
		setvaluesuccess = chConfig.getProperty(propertyname).equals(propertyvalue);
		if(setvaluesuccess)	System.out.println(DebugInfo() + "Successfully updated " + propertyname + " with " + propertyvalue);
        else System.out.println(DebugInfo() + "Unable to update " + propertyname);
	}
	
	protected static boolean isProcessRunning(String serviceName) 
	{
		try
		{
			 Process p = Runtime.getRuntime().exec(TASKLIST);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			 String line;
			 while ((line = reader.readLine()) != null) 
			 {
				 if(DEBUG)System.out.println(DebugInfo() + line);
				 if (line.toLowerCase().startsWith(serviceName.toLowerCase())) 
				 {	
					 System.out.println(DebugInfo() + serviceName + " is running. Killing process now..");
					 return true;	
				 }				  
			 }
			 System.out.println(DebugInfo() + serviceName + " is not running");
			 return false;
		}
		catch (IOException io) {io.printStackTrace();} 
		return false;
	}

	protected static void killProcess() 
	{
		try	
		{
			System.out.println(DebugInfo() + "Retrieved Process Kill List ");
			LIST_PROCESS = new ArrayList(Arrays.asList(getParameter("marimba.interactiveconsole.terminateprocess.list").split("\\s*,\\s*")));
			for (String processname : LIST_PROCESS) 
			{
				if(DEBUG) System.out.println(DebugInfo() + processname);
				if (isProcessRunning(processname)) 
				{
					Runtime.getRuntime().exec(KILL + processname);	
					System.out.println(DebugInfo() + processname + " is terminated");
				}
			}
		}
		catch (IOException io) {io.printStackTrace();}			
	}
	
	//MultiLine Wordwrap
	protected static String convertToMultiline(String originalmessage)
	{
	    return "<html><div style='text-align: center;'>" + originalmessage.replaceAll("\n", "<br>") + "</html>";
	}
	
	protected static String DebugInfo() 
	{
		String currenttime = new java.text.SimpleDateFormat("[dd/MMM/yyyy HH:mm:ss Z] ").format(new Date()); 
		String logtimestamp = currenttime	+ "- Client Engineering Info - " + System.getProperty("user.name") + " ";
		return logtimestamp;
	}
	
	private String getAdapterState(IChannel channelurl)
	{
		String adapterState = null;
		adapterState = channelurl.getProperty("adapter.state");
		if(DEBUG) System.out.println(DebugInfo() + "return value from getAdapterState(): " + adapterState);
		return adapterState;
	}
	
		//Wait for the IActive to die.
	private boolean waitFor(IActive active, long timeout) 
	{
		try 
		{
			Thread timer = new Thread(new MarimbaChannelManagerTimer(timeout, Thread.currentThread()));
			timer.start();
			while (active.getApplicationStatus() != IActive.APP_DEAD) {Thread.sleep(1000);}
			timer.interrupt();
		}
		// bad news: we’ve timed out!
		catch (InterruptedException e)	{	active.kill();	return false;	}
		return true;
	}

	class MarimbaChannelManagerTimer implements Runnable 
	{
		private long to;
		private Thread client;
		public MarimbaChannelManagerTimer(long to, Thread client) 
		{
			this.to = to;
			this.client = client;
		}
		public void run() 
		{
			try {Thread.sleep(to);}			// the client thread will interrupt us when done
			catch (InterruptedException e) {return;	}			// the client thread hasn’t awakened yet, so interrupt it.
			client.interrupt();
		}
	}	
}
