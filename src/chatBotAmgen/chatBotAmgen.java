package chatBotAmgen;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import javax.xml.parsers.ParserConfigurationException;

import org.ejml.simple.SimpleMatrix;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class chatBotAmgen extends JFrame implements KeyListener{

	JPanel p=new JPanel(null);
	JTextArea dialog=new JTextArea(20,48);
	JTextArea input=new JTextArea(2,48);
	JScrollPane scroll=new JScrollPane(
			dialog,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, org.json.simple.parser.ParseException{
		new chatBotAmgen();
	}
	
	public chatBotAmgen() throws IOException{
		super("Chat Bot");
		setSize(550,600);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		DefaultCaret caret = (DefaultCaret)dialog.getCaret();
	    caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);		
		dialog.setEditable(false);
		input.addKeyListener(this);
		BufferedImage img = ImageIO.read(chatBotAmgen.class.getClassLoader().getResourceAsStream("images.png"));
        JLabel label = new JLabel(new ImageIcon(img));
		label.setBounds(0,400, 200, 200);
		scroll.setBounds(530,0,16,400);
		dialog.setBounds(0, 0,530, 397 );
		input.setBounds(200,400,340,600);
		p.add(scroll,dialog);
		p.add(dialog);
		p.add(input);
		p.setBackground(new Color(100,80,10));
		p.add(label);
		add(p);		
		
		setVisible(true);
	}
	
	public void keyPressed(KeyEvent e){
		if(e.getKeyCode()==KeyEvent.VK_ENTER){
			String output_array[]=null;
			input.setEditable(false);
			String quote=input.getText();
			input.setText("");
			addText("-->You:\t"+quote);
			quote.trim();
			analyzeSentiment sent=new analyzeSentiment();
			sent.sentiment(quote);
			try {
				output_array=train(quote);
			} catch (ParserConfigurationException | SAXException | IOException | ParseException e1) {
				e1.printStackTrace();
			}
			if(output_array!=null){
			int rand=(int)Math.floor(Math.random()*output_array.length);
			addText("\n-->Bot\t"+output_array[rand]);}
			else{
				addText("\n-->Bot\tI did not quite get you.");
			}
			addText("\n");
		}
	}
	
	public void keyReleased(KeyEvent e){
		if(e.getKeyCode()==KeyEvent.VK_ENTER){
			input.setEditable(true);
		}
	}
	
	public void keyTyped(KeyEvent e){}
	
	public void addText(String str){
		dialog.setText(dialog.getText()+str);
	}

	public static String[] train(String S) throws ParserConfigurationException, SAXException, IOException, org.json.simple.parser.ParseException
	{
		JSONParser parser = new JSONParser();
		 
        try { 
            Object obj = parser.parse(new FileReader("C:/Users/amishr02/workspace/chatBotAmgen/src/chatBotAmgen/conversation.json"));          
            JSONObject jsonObject = (JSONObject) obj;
            int size=jsonObject.size();
            String temp[]=null;
            double max_match=Double.NEGATIVE_INFINITY;
            for(int i=1;i<=size;i++){
            String var="list"+i;
            JSONObject jsonObject1=(JSONObject)jsonObject.get(var);
            String s=jsonObject1.get("output").toString();
            String output[]=s.split(",");
            s=jsonObject1.get("input").toString();
            String input[]=s.split(",");
            for (String inp: input) 
            	{inp=inp.replaceAll("[^A-Za-z0-9'. ]", "").trim();           	
            	double percent_match=match(inp.toLowerCase(),S.toLowerCase());
            	if(max_match<percent_match)
            	{
            		max_match=percent_match;
            		temp=output.clone();
            	}
                }
            }
            if(max_match>70.0)
            return removeOutputPunctuation(temp);
            else
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
		return null;
    }
	public static String[] removeOutputPunctuation(String output[])
	{
		 for (int j=0;j<output.length;j++){
         	output[j]=output[j].replaceAll("[^A-Za-z0-9'. ]", "").trim();
         }
		 return output;
	}
	public static double similarity(String s1, String s2) {
	    String longer = s1, shorter = s2;
	    if (s1.length() < s2.length()) {
	      longer = s2; shorter = s1;
	    }
	    int longerLength = longer.length();
	    if (longerLength == 0) { return 1.0; }	    
	    return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
	  }
	  public static int editDistance(String s1, String s2) {
	    int[] costs = new int[s2.length() + 1];
	    for (int i = 0; i <= s1.length(); i++) {
	      int lastValue = i;
	      for (int j = 0; j <= s2.length(); j++) {
	        if (i == 0)
	          costs[j] = j;
	        else {
	          if (j > 0) {
	            int newValue = costs[j - 1];
	            if (s1.charAt(i - 1) != s2.charAt(j - 1))
	              newValue = Math.min(Math.min(newValue, lastValue),
	                  costs[j]) + 1;
	            costs[j - 1] = lastValue;
	            lastValue = newValue;
	          }
	        }
	      }
	      if (i > 0)
	        costs[s2.length()] = lastValue;
	    }
	    return costs[s2.length()];
	  }

	  public static double match(String s, String t) {
	    return (similarity(s, t)*100);
	  }	  
}

class NLP {
static StanfordCoreNLP pipeline;

public static void init() {
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
    pipeline = new StanfordCoreNLP(props);
}

public static int findSentiment(String tweet) {

    int mainSentiment = 0;
    if (tweet != null && tweet.length() > 0) {
        int longest = 0;
        Annotation annotation = pipeline.process(tweet);
        for (CoreMap sentence : annotation
                .get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence
                    .get(SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            SimpleMatrix sentiment_new = RNNCoreAnnotations.getPredictions(tree);             
            String partText = sentence.toString();
            if (partText.length() > longest) {
                mainSentiment = sentiment;
                longest = partText.length();
            }
        }
    }
    return mainSentiment;
    }
}

class analyzeSentiment {
    public static void sentiment(String S) {
        ArrayList<String> tweets = new ArrayList<String>();
        tweets.add(S);
        NLP.init();
        for(String tweet : tweets) {
            int mainSentiment= NLP.findSentiment(tweet);
            if (mainSentiment == 2 || mainSentiment > 4 || mainSentiment < 0) {
                System.out.println("Neutral");
            }
            else if (mainSentiment > 2) {
                System.out.println("Good");
            }
            else {
                System.out.println("Bad");
            }
        }
    }
}