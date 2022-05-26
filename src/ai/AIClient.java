package ai;

import java.io.*;
import java.net.*;
import java.util.Date;
import javax.swing.*;

import java.awt.*;
import kalaha.*;

public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    	
    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    	
    public static int count_increment=2; // initial level and it goes on incrementing...
	public int ai_player=player;
	//Assign P1 and P2 status 
	public double maximum_time_in_seconds=5;
	//Time limit
	
	//This method takes the gamestate as an argument and runs the MinMax algo with alpha beta pruning 
	public int getMove(GameState cb) {
		
		boolean isMaxPlayer=(ai_player==1)?true:false;
		
//		int level=8; // The level upto which we will run the search algo
		
		BestMove bms=new BestMove();
		
		if(isMaxPlayer)
		{
			bms.score=-100000;
		}
		else {
			bms.score=100000;
		}
		
		
		Date now = new Date();
        long linux_timestamp = now.getTime();
        
		
        while (maximum_time_in_seconds>= ((double) (System.currentTimeMillis() - linux_timestamp) / (double) 1000)) {
        	
			System.out.println("Seconds Outside::"+((System.currentTimeMillis() - linux_timestamp) / (double) 1000));
			
        	count_increment++;
        
        	BestMove bm = algoImp(linux_timestamp,cb, count_increment, isMaxPlayer, -100000,
					100000);
			
			if(isMaxPlayer)
			{
				if(bm.score>bms.score)
				{
					bms.ambo=bm.ambo;
					bms.score=bm.score;
				}
			}
			else
			{
				if(bm.score<bms.score)
				{
					bms.ambo=bm.ambo;
					bms.score=bm.score;
				}
			}	
        	
        }
        
        count_increment=0;
        
		System.out.println("Best Score::"+bms.score);
		return bms.ambo;
	}
	
    //This method takes the time, gamesstate, level, whether we are min or max player and the alpha beta values to run the actual implementation of minmax algo with alpha beta pruning
	public BestMove algoImp(long linux_timestamp,GameState gameState, int level, boolean isMaxPlayer, int alpha, int beta) {

		int best_move = -1;
		
		BestMove bm_temp=new BestMove();
		
		Date now = new Date();
        double seconds = ((double)now.getTime()-(double)linux_timestamp)/(double)1000;
		
        System.out.println("Seconds inside loop::"+seconds);
        
		if(seconds<=maximum_time_in_seconds) // it will even check the time condition inside the algo
		{
			if ( gameState.gameEnded() || level == 0 ) {
				//stopping condition for algorithm
					
				BestMove bm=new BestMove();
				
				if(isMaxPlayer)
				{
					bm.score=gameState.getScore(1) - gameState.getScore(2);
				}
				else
				{
					bm.score=gameState.getScore(2) - gameState.getScore(1);
				}
				bm_temp=bm;// stored in temporary variable 
				return bm;
			}
		}
		else
		{
			// if time exceeds it returns the previous score.
			return bm_temp;
		}
		
		
		if (isMaxPlayer) {

			int max_negitive = -100000;

			for(int j=1;j<7;j++){

				if (gameState.moveIsPossible(j)) {

					GameState game_state = gameState.clone();
					game_state.makeMove(j);
					//checks if it is possible to make a move (if you have some bonus chance or not)
					
					boolean isMaxPlayer_next=(game_state.getNextPlayer()==1)?true:false;
					
					BestMove bm = algoImp(linux_timestamp,game_state, level - 1, isMaxPlayer_next, alpha, beta);

					if (bm.score > max_negitive) {
						best_move = j;
						max_negitive = bm.score;
					}

					alpha=bm.score > alpha?bm.score:alpha;
					
					if (beta <= alpha)
						break;

				}

			}
			BestMove bm=new BestMove();
			bm.score=max_negitive;
			bm.ambo=best_move;

			return bm;

		} else {

			int min_positive = 100000;
			for(int k=1;k<7;k++) {
				if (gameState.moveIsPossible(k)) {

					GameState game_state = gameState.clone();
					game_state.makeMove(k);
					
					boolean isMaxPlayer_next=(game_state.getNextPlayer()==1)?true:false;
					
					BestMove bm = algoImp(linux_timestamp,game_state, level - 1, isMaxPlayer_next, alpha, beta);

					if (bm.score < min_positive) {
						best_move = k;
						min_positive = bm.score;
					}

					beta=bm.score < beta?bm.score:beta;

					if (beta <= alpha)
						break;

				}
			}
			BestMove bm=new BestMove(); 
			bm.ambo=best_move;
			bm.score=min_positive;
			return bm;
		}

	}
    
    /**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */
    public int getRandom()
    {
        return 1 + (int)(Math.random() * 6);
    }
    
}

class BestMove{
	int score; //The difference between the 2 houses 
	int ambo;
}
