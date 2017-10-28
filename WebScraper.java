import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;




public class WebScraper {
	
	static String mainPageUrl = "https://www.nps.gov/nagpra/FED_NOTICES/NAGPRADIR/index.html";
	
	
	public class UrlObject {
		String date  = null;
		String link  = null;
		String title = null;
		String state = null;
	}
	
	
	private UrlObject[] getMainPageSourceFromFile(String filePath) throws IOException{
		
		UrlObject[] mainObject = new UrlObject[1000];
		UrlObject linkObject = new UrlObject();
		int linkNumber = 0;
		int lineNumber = 0;
		
		try {

			BufferedReader newBufferdReader = new BufferedReader(new FileReader(filePath));
			StringBuilder newStringReader = new StringBuilder();
			String line = newBufferdReader.readLine();
			
			//While we have more lines in the file
			while(line != null) {
				
				newStringReader.append(line);
				newStringReader.append(System.lineSeparator());
				
				//Opperate on lines
				lineNumber++;
	        	
	        	if(lineNumber % 50 == 0) {
	        		System.out.println("50 Lines!");
	        	}
	        	if(lineNumber == 247) {
	        		System.out.println("Break!");
	        	}
	        	if(lineNumber == 395) {
	        		System.out.println("Break!");
	        	}
	        	
	        	System.out.println(lineNumber + " " + line);
	        	
	        	//DO stuff to get the page!
	        	//Oh Snap we found the first key word
	        	boolean match_a = line.contains("https://federalregister.gov/a/");
				boolean match_d = line.contains("https://www.federalregister.gov/d/");
				boolean match_articals = line.contains("https://www.federalregister.gov/articles/");
				boolean match_special = line.contains("<td width=\"136\">");
				
	        	
				//We found a link!
				if((match_d == true) || (match_a == true) || (match_articals == true)) {
					
					//Make sure we only have one entry we are processing at a time
					String[] fullLine = line.split("<li>");
					
					//For all the links in one line
					for(int i = 0; i < fullLine.length; i++) {
						
						//Build a temp object
						UrlObject myObj = new UrlObject();

						String[] lineArray = fullLine[i].split("\"");
						
						if(lineArray.length == 3) { 
							String result = lineArray[0];
							String[] splitOne = result.split("<");
							
							myObj.date = splitOne[0];
							myObj.link = lineArray[1];
							myObj.title = fullLine[i];
						}
						else if( lineArray.length == 5) {
							String result = lineArray[0];
							String[] splitOne = result.split("<");
							
							myObj.date = splitOne[0];
							myObj.link = lineArray[1];
							myObj.title = fullLine[i];
						}
						else if( lineArray.length == 7) {
							String result = lineArray[4];
							String[] splitOne = result.split("<");
							
							myObj.date = splitOne[0];
							myObj.link = lineArray[5];
							myObj.title = fullLine[i];
						}
						else {
							//System.out.println("WARNING: Lenght Miss match!!!");
							//System.out.println(inputLine);
							continue;
						}
						
						//Find and assign the state
						linkObject = findAndAssignState(myObj);
						
						if(linkObject.state.matches("skipped")) {
							//System.out.println("NOT_Adding: " + fullLine[i]);
						}
						else {
							//Add the link object to the main list of links
							System.out.println("ADDING LINE: " + lineNumber + ", LINK NUMBER: " + linkNumber);
							mainObject[linkNumber] = linkObject;
							linkNumber++;
						}
					}
				}
				else if (match_special == true) {
					//Build a temp object
					UrlObject myObj = new UrlObject();
					
					//Get the date
					String[] splitOne = line.split(">");
					String[] splitTwo = splitOne[1].split("<");
					myObj.date = splitTwo[0];
					
					//Get the next line with link and state
					line = newBufferdReader.readLine();
					lineNumber++;
					System.out.println(lineNumber + " " + line);
					//Extract data
					boolean match_special_2 = line.contains("<td width=\"677\">");
					if(!match_special_2) {
						System.out.println("WARNING!!!!: next line did not contain correct text");
					}
					
					String[] lineArray = line.split("\"");
					myObj.link = lineArray[3];
					myObj.title = line;
					
					//Find and assign the state
					linkObject = findAndAssignState(myObj);
					
					if(linkObject.state.matches("skipped")) {
						//System.out.println("NOT_Adding: " + fullLine[i]);
					}
					else {
						//Add the link object to the main list of links
						System.out.println("ADDING LINE: " + lineNumber + ", LINK NUMBER: " + linkNumber);
						mainObject[linkNumber] = linkObject;
						linkNumber++;
					}

				}
				else {
					//No match do nothing
				}
				//read the next line
		        line = newBufferdReader.readLine();
			}
		
			newBufferdReader.close();

		} catch (FileNotFoundException e) {
			System.out.printf("Failed to open file %s", filePath);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Return Object
		return mainObject;
	}
	
	@SuppressWarnings("unused")
	private UrlObject[] getMainPageSourceFromURL(String sURL) throws IOException{
		
		UrlObject[] mainObject = new UrlObject[100];
		UrlObject linkObject = new UrlObject();
		int linkNumber = 0;
		int lineNumber = 0;
		
		 URL url = new URL(sURL);
	        URLConnection urlCon = url.openConnection();
	        urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
	        BufferedReader in = null;

	        if (urlCon.getHeaderField("Content-Encoding") != null
	                && urlCon.getHeaderField("Content-Encoding").equals("gzip")) {
	            in = new BufferedReader(new InputStreamReader(new GZIPInputStream(
	                    urlCon.getInputStream())));
	        } else {
	            in = new BufferedReader(new InputStreamReader(
	                    urlCon.getInputStream()));
	        }

	        String inputLine;
	        StringBuilder sb = new StringBuilder();

	        while ((inputLine = in.readLine()) != null) {
	        	
	        	lineNumber++;
	        	
	        	if(lineNumber % 50 == 0) {
	        		System.out.println("50 Lines!");
	        	}
	        	if(lineNumber == 168) {
	        		System.out.println("Break!");
	        	}
	        	
	        	System.out.println(lineNumber + " " + inputLine);
	        	
	        	//DO stuff to get the page!
	        	//Oh Snap we found the first key word
	        	boolean match_a = inputLine.contains("https://federalregister.gov/a/");
				boolean match_d = inputLine.contains("https://www.federalregister.gov/d/");
				boolean match_articals = inputLine.contains("https://www.federalregister.gov/articles/");
				boolean match_special = inputLine.contains("<td width=\"677\">");
				
	        	
				//We found a link!
				if((match_d == true) || (match_a == true)) {
					
					//Make sure we only have one entry we are processing at a time
					String[] fullLine = inputLine.split("<li>");
					
					//For all the links in one line
					for(int i = 0; i < fullLine.length; i++) {
						
						//Build a temp object
						UrlObject myObj = new UrlObject();

						String[] lineArray = fullLine[i].split("\"");
						
						if(lineArray.length == 3) {
							String result = lineArray[0];
							String[] splitOne = result.split("<");
							
							myObj.date = splitOne[0];
							myObj.link = lineArray[1];
							myObj.title = fullLine[i];
						}
						else if( lineArray.length == 7) {
							String result = lineArray[4];
							String[] splitOne = result.split("<");
							
							myObj.date = splitOne[0];
							myObj.link = lineArray[5];
							myObj.title = fullLine[i];
						}
						else {
							//System.out.println("WARNING: Lenght Miss match!!!");
							//System.out.println(inputLine);
							continue;
						}
						
						//Find and assign the state
						linkObject = findAndAssignState(myObj);
						
						if(linkObject.state.matches("skipped")) {
							//System.out.println("NOT_Adding: " + fullLine[i]);
						}
						else {
							//Add the link object to the main list of links
							System.out.println("ADDING LINE: " + lineNumber);
							mainObject[linkNumber] = linkObject;
							linkNumber++;
						}
					}
				}
				else if (match_articals == true) {
					//Match Artical
					System.out.println("Artical Match");
				}
				else if (match_special == true) {
					//parse special 2014 links
					System.out.println("Special Match");
				}
				else {
					//No match do nothing
				}
				
				
	        	//sb.append(inputLine);
	        }
	        in.close();
		
		return mainObject;
	}
	
	public UrlObject findAndAssignState( UrlObject inputUrlObj) {
		

		//Build a temp object
		UrlObject myObj = new UrlObject();
		
		//Make a copy of the input object
		myObj = inputUrlObj;
		
		//Find the correct state and populate the state variable
		//Find Arizona
		if(inputUrlObj.title.contains("Arizona") || inputUrlObj.title.contains("AZ")) {
			myObj.state = "Arizona";
		}

		//Find Colorado
		else if(inputUrlObj.title.contains("Colorado") || inputUrlObj.title.contains("CO") ) {
			myObj.state = "Colorado";
		}

		//Find Montana
		else if(inputUrlObj.title.contains("Montana") || inputUrlObj.title.contains("MT")) {
			myObj.state = "Montana";
		}

		//Find Nevada
		else if(inputUrlObj.title.contains("Nevada") || inputUrlObj.title.contains("NV")) {
			myObj.state = "Nevada";
		}

		//Find New Mexico
		else if(inputUrlObj.title.contains("New Mexico") || inputUrlObj.title.contains("NM")) {
			myObj.state = "New Mexico";
		}

		//Find Oklahoma
		else if(inputUrlObj.title.contains("Oklahoma") || inputUrlObj.title.contains("OK")) {
			myObj.state = "Oklahoma";
		}

		//Find Texas
		else if(inputUrlObj.title.contains("Texas") || inputUrlObj.title.contains("TX")) {
			myObj.state = "Texas";
		}

		//Find Utah
		else if(inputUrlObj.title.contains("Utah") || inputUrlObj.title.contains("UT")) {
			myObj.state = "Utah";
		}

		//Find Wyoming
		else if(inputUrlObj.title.contains("Wyoming") || inputUrlObj.title.contains("WY")) {
			myObj.state = "Wyoming";
		} 
		else {
			myObj.state = "skipped";
		}
		
		return myObj;
	}
	
	@SuppressWarnings("unused")
	private UrlObject getSinglePageSource(String sURL) throws IOException {

		int followerCount     = 0;
		int postsCount        = 0;
		UrlObject retValues   = null;
		boolean followersNext = false;
		boolean postsNext     = false;

        URL url = new URL(sURL);
        URLConnection urlCon = url.openConnection();
        urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        BufferedReader in = null;

        if (urlCon.getHeaderField("Content-Encoding") != null
                && urlCon.getHeaderField("Content-Encoding").equals("gzip")) {
            in = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    urlCon.getInputStream())));
        } else {
            in = new BufferedReader(new InputStreamReader(
                    urlCon.getInputStream()));
        }

        String inputLine;
        StringBuilder sb = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
        	
        	//Logic to isolate just the followers and daily posts numbers
        	//Extracts the follower count from the board
        	if(followersNext == true) {
      
        		String[] splitOne = inputLine.split(">");
				String[] splitTwo = splitOne[1].split("<");
				//remove commas from the string
				splitTwo[0] = splitTwo[0].replace(",", "");
				
				if(splitTwo[0].equals("") ) {
				}
				else {
					followerCount = Integer.parseInt(splitTwo[0]);
	        		//retValues[0] = followerCount;
	        		//System.out.println(retValues[0]);
				}
        		followersNext = false;
        	}
        	//Extracts the total posts of the day from the board
        	if(postsNext == true) {
        		
        		String[] splitOne = inputLine.split("<");
        		
        		//remove commas from the string
				splitOne[0] = splitOne[0].replace(",", "");
				splitOne[0] = splitOne[0].replace(" ", "");
  		
				if(splitOne[0].equals("") ) {
				}
				else {
					postsCount = Integer.parseInt(splitOne[0]);
	        		//retValues[1] = postsCount;
	        		//System.out.println(retValues[0]);
				}
        		postsNext = false;
        	}
        	//Checks to see if the next line is either the follower count or the posts today count
        	if(inputLine.contains("Followers:")) {
        		followersNext = true;
        	}
        	if(inputLine.contains("Posts Today:")) {
        		postsNext = true;
        	}

        	sb.append(inputLine);
        }
        in.close();
        
        //return sb.toString();
        return retValues;
	}
	
	public static void createDirecotry(String dirName) {
		File theDir = new File(dirName);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    System.out.println("creating directory: " + theDir.getName());
		    boolean result = false;

		    try{
		        theDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		    if(result) {    
		        System.out.println("DIR created");  
		    }
		}
	}
	
	public static void printUrlInfo(UrlObject[] inputTickerObj, String type, String recordTime) {

		String myFileName = "output/" + recordTime + "_" + type + ".txt";

		FileWriter mainFile       = null;
		FileWriter tickerFile     = null;
		PrintWriter myPrintWriter = null;
		PrintWriter myTickerWriter = null;
		
		//If there is no directory for output, create it
		createDirecotry("output");

		try {
			mainFile = new FileWriter(myFileName);
			
			//myBuffWriter = new BufferedWriter(myFileWriter);
			myPrintWriter = new PrintWriter(mainFile);
		
			//File output format 2
			//myPrintWriter.printf("Ticker, Top50Pos, FollwerCount, DailyPosts, ReadCount, TickerVolume, StarCount\n");
			myPrintWriter.printf("Ticker, BoardPos, FollwerCount, DailyPosts, StarCount\n");
			for(int i = 0; i < inputTickerObj.length; i ++) {
				
				//Log to the main file
				myPrintWriter.printf("%s\n", inputTickerObj[i].link);

			}
			
		}
	    catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (myPrintWriter != null)
					myPrintWriter.close();
				if (myTickerWriter != null)
					myTickerWriter.close();
				if (mainFile != null)
					mainFile.close();
				if (tickerFile != null)
					tickerFile.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static void printMainPageInfo(UrlObject[] mainList) {
		String myFileName = "output/MainLinkList.txt";

		FileWriter mainFile       = null;
		FileWriter tickerFile     = null;
		PrintWriter myPrintWriter = null;
		PrintWriter myTickerWriter = null;
		
		//If there is no directory for output, create it
		createDirecotry("output");

		try {
			mainFile = new FileWriter(myFileName);
			//myBuffWriter = new BufferedWriter(myFileWriter);
			myPrintWriter = new PrintWriter(mainFile);
		
			//File output format 2

			for(int i = 0; i < mainList.length; i ++) {
				
				if(mainList[i] == null) {
					break;
				}
				
				//Log to the main file
				myPrintWriter.printf("%s,%s,%s\n", mainList[i].state,
												   mainList[i].date,
												   mainList[i].link);
			}
			
		}
	    catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (myPrintWriter != null)
					myPrintWriter.close();
				if (mainFile != null)
					mainFile.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static int getRandomNumber(int a, int b) {
	    if (b < a)
	        return getRandomNumber(b, a);
	    return a + (int) ((1 + b - a) * Math.random());
	}
	
	public static void main(String[] args) {
		
		
		
	
		try {
			
		
			SimpleDateFormat dateFormatTest = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

			Date postDateObj = dateFormatTest.parse("09/28/2017 09:46:36 AM");
			DateTime postDate = new DateTime(postDateObj);
			
			DateTime currentDate = new DateTime();
			
			currentDate = currentDate.minusHours(36);
			
			boolean myTestAfter = postDate.isAfter(currentDate);

			boolean myTestBefore = postDate.isBefore(currentDate);
			
			System.out.println(myTestBefore);
			
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		
		////////////////////////////////////////////////////////////////////////////////
		
		System.out.println("Lets get some LIIINKS");
		
		NJ_ForReal mainObj = new NJ_ForReal();
		UrlObject[] urlList = new UrlObject[100];
		
		//If there is no directory for output, create it
		createDirecotry("output");

		//Get the execution time, for output files
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss") ;
		Date date = new Date();
		String startTime = dateFormat.format(date);

		//////////////////////////////////////////////////////////////////////////////
		/// Setup the output window
		
		
		final JFrame theFrame = new JFrame();
        theFrame.setTitle("Trick Or Treat!");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //theFrame.setSize(1100, 1800);
        theFrame.setPreferredSize(new Dimension(800,850));
        
        theFrame.setLocation(100, 100);

        //Create the panel
        JPanel mainPanel = new JPanel();
        //Create the text area
        JTextArea theText = new JTextArea(50,68);
        theText.append("--- Trick Or Treat..... for the better things in life!\n"); 
        
        //Create the scroll pane
        JScrollPane jScrollPane = new JScrollPane(theText);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        mainPanel.add(jScrollPane);
        theFrame.getContentPane().add(mainPanel);
        theFrame.pack();
        theFrame.setVisible(true);
        
		///
		/////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////
		/// Execute the data acquisition 

		/////////////////////////////////////////////////////////////////////////////
		///Pull the data
        
        //String fileName = "C:/Users/Erik/eware/FightThePower/Inventory_Completion_Database_HTML.html";
        String fileName = "C:/Users/Erik/eware/FightThePower/new_input_file_test.html";
        
        theText.append("----- Pulling Data -----\n"); 
        //Fetch the data from the top 50c
		try {
			//urlList = mainObj.getMainPageSourceFromURL(mainPageUrl);
			urlList = mainObj.getMainPageSourceFromFile(fileName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//Random wait so we don't get banned
		try {
			int rand = getRandomNumber(1000, 3000);
			Thread.sleep(500 + rand);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Print the data
		printMainPageInfo(urlList);
		
		theText.append(" Ok Cash Gotten! \n");
		//System.out.println("Ok Cash Gotten!");
		
		//Exit Java
		System.exit(0);
	}

}
















