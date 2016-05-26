package chatBotAmgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

public class chatBotAmgen extends JFrame implements KeyListener{

	JPanel p=new JPanel(null);
	JTextArea dialog=new JTextArea(20,48);
	JTextArea input=new JTextArea(2,48);
	JScrollPane scroll=new JScrollPane(
		dialog,
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
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
		
		dialog.setEditable(false);
		input.addKeyListener(this);
		BufferedImage img = ImageIO.read(chatBotAmgen.class.getClassLoader().getResourceAsStream("images.png"));
        JLabel label = new JLabel(new ImageIcon(img));
		
		p.add(scroll);
		label.setBounds(0,400, 200, 200);
		dialog.setBounds(0, 0,590, 397 );
		input.setBounds(200,400,590,600);
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
	
	public boolean inArray(String in,String[] str){
		boolean match=false;
		for(int i=0;i<str.length;i++){
			if(str[i].equals(in)){
				match=true;
			}
		}
		return match;
	}
	
	public static String[] train(String S) throws ParserConfigurationException, SAXException, IOException, org.json.simple.parser.ParseException
	{
		JSONParser parser = new JSONParser();
		 
        try { 
            Object obj = parser.parse(new FileReader("C:/Users/amishr02/workspace/chatBotAmgen/src/chatBotAmgen/conversation.json"));          
            JSONObject jsonObject = (JSONObject) obj;
            int size=jsonObject.size();
            for(int i=1;i<=size;i++){
            String var="list"+i;
            JSONObject jsonObject1=(JSONObject)jsonObject.get(var);
            String s=jsonObject1.get("output").toString();
            String output[]=s.split(",");
            s=jsonObject1.get("input").toString();
            String input[]=s.split(",");
            for (String inp: input) 
            	{inp=inp.replaceAll("[^A-Za-z0-9'. ]", "").trim();
                if (inp.equals(S))
                	return removeOutputPunctuation(output);}
            }
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
}