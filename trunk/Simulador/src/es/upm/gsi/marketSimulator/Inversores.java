package es.upm.gsi.marketSimulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.ascape.model.CellOccupant;
import org.ascape.model.Agent;
import org.ascape.model.Scape;

public class Inversores extends CellOccupant {	
	private static final long serialVersionUID = -3651394341932621403L;
	/*
	 * Common of all investor. I think already I don't need it.
	 */
	protected static int iteraciones = 0;
	private static int time = 0;
	protected static boolean isNewIteration = true;
	protected static int cuenta = 0;
	protected static int setId = 1;
	protected static int EXPERIMENTED_INVESTOR = 0;
	protected static int AMATEUR_INVESTOR = 1;
	protected static int RANDOM_INVESTOR = 2;
	
	private InvestorType investor;
	
	protected static int FREQUENT_USER = 0;
	protected static int OCASIONAL_USER = 1;
	protected static int GOOD_WRITER = 0;
	protected static int BAD_WRITER = 1;
	
	protected static int FRIENDLY_USER = 0;
	protected static int NO_FRIENDLY_USER = 1;
	
	private int tipoAgente[];
	private int id;
	protected Color myColor;
	private int played = 0;
	protected ArrayList<Message> misMensajes;	
	
	private int messageHistory[];
	private int readerHistory[];
	private int uniqueReaderHistory[];
	private int followerHistory[];
	private int uniqueFollowerHistory[];
	private int scorerHistory[];
	private int scoreHistory[];
	
	private int sizeMessageByLimit = 0;
	
	private HashSet<Inversores> friends;
	private HashSet<Inversores> followers;
	
	//messages and comments probabilities:
	private double probabilidadLeer;
	private double probabilidadComentar;
	private double probabilidadPostear;
	private double scoreProbability;	
	private double probabilidadBuenMensaje; //good message probability
	private double friendlyProbability;
	
	//reputations:
	//private double popularity = 0;		
	private double messageReputation[];
	private double friendReputation = 0;
	
	
	//create the list of rules for scape
	public void scapeCreated() {
		getScape().addInitialRule(INITIALIZE_RULE);
		getScape().addInitialRule(MOVE_RANDOM_LOCATION_RULE);
		//method update
		getScape().addRule(ITERATE_RULE);
		getScape().addRule(UPDATE_RULE);
		getScape().addRule(RANDOM_WALK_RULE);
		//getScape().addRule(PLAY_RANDOM_NEIGHBOR_RULE);
	}

	/*
	 * One way or other is right. You can define the rule in SimulateSocialExchange class
	 * or here with method initialize
	 */
	
    public void eligeInversor() {
    	setId(setId++);
    	misMensajes = new ArrayList<Message>();
    	friends = new HashSet<Inversores>();
    	followers = new HashSet<Inversores>();
    	tipoAgente = new int[4];
    	messageReputation = new double[2];    	
    	messageHistory = new int[]{0,0};
    	readerHistory = new int[]{0,0};
    	uniqueReaderHistory = new int[]{0,0};
    	followerHistory = new int[]{0,0};
    	uniqueFollowerHistory = new int[]{0,0};
    	scorerHistory = new int[]{0,0};
    	scoreHistory = new int[]{0,0};
    	//Investor:
    	double totalProbability = Properties.INTELLIGENT_INVESTOR_PROBABILITY +
    		Properties.AMATEUR_INVESTOR_PROBABILITY + Properties.RANDOM_INVESTOR_PROBABILITY;	
    	double investorType = randomInRange(0.0, totalProbability);
        if (investorType < Properties.INTELLIGENT_INVESTOR_PROBABILITY) {
            tipoAgente[0] = EXPERIMENTED_INVESTOR;            
            investor = new IntelligentInvestor(this);
            //aggressive investor
            //iteraccionesCompra = 3;
            //iteracionesVenta = 2;
            //calm investor            
        } else if(investorType < (Properties.INTELLIGENT_INVESTOR_PROBABILITY +
        		Properties.AMATEUR_INVESTOR_PROBABILITY)) {
        	tipoAgente[0] = AMATEUR_INVESTOR;
        	investor = new AmateurInvestor(this);
        	/*//calm investor
            iteraccionesCompra = 4;
            iteracionesVenta = 5;
            setLiquidez(Properties.INITIAL_LIQUIDITY/4); //setLiquidez(randomInRange(1000,3000));
            maxValorCompra = getLiquidez()*0.1;
            actividadComprar = Properties.BUY_PROBABILITY;
            actividadVender = 0.4;
            //if one share along the movements decrease his value in 22% you might buy
            rentabilidadCompra = 0.22;
            //if one share along the movements increase his value in 17% you may sell
            //rentabilidadVenta = 0.17;*/        	           
        } else {
        	tipoAgente[0] = RANDOM_INVESTOR;
        	investor = new RandomInvestor(this);
        }
        //Activity of the user:
        if(randomInRange(0.0, 1.0) < Properties.FREQUENT_USER_PROBABILITY) {
        	tipoAgente[1] = FREQUENT_USER;
        	probabilidadLeer = randomInRange(Properties.FREQ_USER_READ_PROBABILITY_LIMITS[0],
        			Properties.FREQ_USER_READ_PROBABILITY_LIMITS[1]);
        	probabilidadComentar = randomInRange(Properties.FREQ_USER_COMMENT_PROBABILITY_LIMITS[0],
        			Properties.FREQ_USER_COMMENT_PROBABILITY_LIMITS[1]);
        	probabilidadPostear = randomInRange(Properties.FREQ_USER_POST_PROBABILITY_LIMITS[0],
        			Properties.FREQ_USER_POST_PROBABILITY_LIMITS[1]);
        	scoreProbability = randomInRange(Properties.FREQ_USER_SCORE_PROBABILITY_LIMITS[0],
        			Properties.FREQ_USER_SCORE_PROBABILITY_LIMITS[1]);
        	//probabilidadLeer = randomInRange(0.6,0.9);
        	//probabilidadComentar = randomInRange(0.2,0.36);
        	//probabilidadPostear = randomInRange(0.08,0.14);
        	//scoreProbability = randomInRange(0.1,0.34);
        } else {
        	tipoAgente[1] = OCASIONAL_USER;
        	probabilidadLeer = randomInRange(Properties.OCA_USER_READ_PROBABILITY_LIMITS[0],
        			Properties.OCA_USER_READ_PROBABILITY_LIMITS[1]);
        	probabilidadComentar = randomInRange(Properties.OCA_USER_COMMENT_PROBABILITY_LIMITS[0],
        			Properties.OCA_USER_COMMENT_PROBABILITY_LIMITS[1]);
        	probabilidadPostear = randomInRange(Properties.OCA_USER_POST_PROBABILITY_LIMITS[0],
        			Properties.OCA_USER_POST_PROBABILITY_LIMITS[1]);
        	scoreProbability = randomInRange(Properties.OCA_USER_SCORE_PROBABILITY_LIMITS[0],
        			Properties.OCA_USER_SCORE_PROBABILITY_LIMITS[1]);
        	//probabilidadLeer = randomInRange(0.05,0.3);
        	//probabilidadComentar = randomInRange(0.03,0.08);
        	//probabilidadPostear = randomInRange(0.01,0.04);
        	//scoreProbability = randomInRange(0.1,0.3);
        }
        //Good o bad writer?
        if(randomInRange(0.0, 1.0) < Properties.GOOD_WRITER_PROBABILITY) {
        	tipoAgente[2] = GOOD_WRITER;
        	probabilidadBuenMensaje = randomInRange(Properties.GOOD_MESSAGES_PROBABILITY_LIMITS[0],
        			Properties.GOOD_MESSAGES_PROBABILITY_LIMITS[1]);
        } else {
        	tipoAgente[2] = BAD_WRITER;
        	probabilidadBuenMensaje = randomInRange(Properties.BAD_MESSAGES_PROBABILITY_LIMITS[0],
        			Properties.BAD_MESSAGES_PROBABILITY_LIMITS[1]);
        }
        //Friendly or not_friendly
        if(randomInRange(0.0, 1.0) < Properties.FRIENDLY_PROBABILITY) {
        	tipoAgente[3] = FRIENDLY_USER;
        	friendlyProbability = randomInRange(Properties.FRIENDLY_PROBABILITY_LIMITS[0],
        			Properties.FRIENDLY_PROBABILITY_LIMITS[1]);
        } else {
        	tipoAgente[3] = NO_FRIENDLY_USER;
        	friendlyProbability = randomInRange(Properties.NO_FRIENDLY_PROBABILITY_LIMITS[0],
        			Properties.NO_FRIENDLY_PROBABILITY_LIMITS[1]);
        }
        setColorByAgentType();
               
    }
   
    private void setColorByAgentType () {
    	float red;
    	float green;
    	float blue;
    	if(tipoAgente[0] == EXPERIMENTED_INVESTOR) {
    		red = 0;
    	} else if(tipoAgente[0] == AMATEUR_INVESTOR){
    		red = 0.5f;
    	} else
    		red = 1;
    	if(tipoAgente[1] == FREQUENT_USER) {
    		green = 0;
    	} else {
    		green = 0.8f;
    	}
    	if(tipoAgente[2] == GOOD_WRITER) {
    		blue = 0;
    	} else {
    		blue = 1;
    	}
    	setColor(new Color(red, green, blue));	
    }
        
	public void iterate(){
		// Ascape must just call it one time per iteration of the first agent...
		/*
		 * I think already I don't need it.
		 */
		if ((iteraciones % ((SimulateSocialExchange)getRoot()).getnInversores() == 0) 
				&& isNewIteration ){
			isNewIteration = false;
			time++;
			//Generate popularity and reputation of investor�messages
			for(int i = 0; i < ((SimulateSocialExchange)getRoot()).getPopularMessages().length; i++)
				((SimulateSocialExchange)getRoot()).getPopularMessages()[i].clear();
			for(Object cell : scape) {
				if(cell instanceof Inversores) {
					for(Message message : ((Inversores)cell).getMensajes()) {
						if((time - message.getDate()) < Properties.TIME_LIMIT) {
							message.generateReputation(time);
							((SimulateSocialExchange)getRoot()).sortAddMessages(message);
						}
					}					
				}
			}
			for(int i = 0; i < ((SimulateSocialExchange)getRoot()).getPopularMessages().length; i++) {
				int sizePopularMessages = ((SimulateSocialExchange)getRoot()).getPopularMessages()[i].size();
				int initMessage = (int) Math.ceil(sizePopularMessages/Properties.POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR);
				for(int j = initMessage; j < sizePopularMessages; j++) {
					//((SimulateSocialExchange)getRoot()).getPopularMessages().get(i).setPopularity
					//	((double)i/sizePopularMessages*Message.POPULARITY_INCREMENTATION_LINEAL_FACTOR);
					((SimulateSocialExchange)getRoot()).getPopularMessages()[i].get(j).setPopularity
						(i,Math.pow(Properties.POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR, 
						 j*Properties.POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR/sizePopularMessages-1));
				}
			}
			if(time % Properties.STATISTICS_INTERVAL == 0) { //Generate statistics
				System.out.println("Estadistica en intervalo "+time+":");
				Ibex35 ibex35 = ((SimulateSocialExchange)getRoot()).getStock();				
				//Generate activity reputation and financial reputation of the investors
				for(Object cell : scape) {
					if(cell instanceof Inversores) {
						((Inversores)cell).generateActivityReputation();
						((Inversores) cell).investor.updateFinancialReputation(ibex35);
					}					
				}
				double intelligentStatistics[][][] = new double[5][2][7]; 
					//0=IMP,1=PER,2=ANX,3=MEM,4=DIV; 0=BUY,1=SELL,2=NUM,3=ROI,4=CAP,5=LIQ,6=CapNegReturn
				double investorStatistics[][] = new double[3][11]; 
					//0=EXP_INV,1=AMA_INV,2=RA_INV; 0=BUY,1=SELL,2=NUM,3=ROI,4=CAP,5=liquidity,
					//  6=capitalWithNegativeReturn,7=buyProfibility,sellRange,X	
				((SimulateSocialExchange)getRoot()).setSortInvestorByFinance(sortByFinancialReputation(scape));
				for(int i = 0; i < ((SimulateSocialExchange)getRoot()).getSortInvestorByFinance().size(); i++) {
					Inversores cell = ((SimulateSocialExchange)getRoot()).getSortInvestorByFinance().get(i);
					double capital = cell.investor.getActualCapital(ibex35);
					cell.investor.addCapitalToHistory(capital);
					
					System.out.println("  id:" + cell.getId() + cell.getAgentTypeToString() + " with financial reputation:" +
							cell.investor.getFinancialReputation() );
					investorStatistics[cell.getTipoAgente()[0]][0] += cell.investor.buys;
					investorStatistics[cell.getTipoAgente()[0]][1] += cell.investor.sells;
					investorStatistics[cell.getTipoAgente()[0]][2]++;
					investorStatistics[cell.getTipoAgente()[0]][3] += cell.investor.getFinancialReputation();					
					investorStatistics[cell.getTipoAgente()[0]][4] += capital;
					investorStatistics[cell.getTipoAgente()[0]][5] += cell.investor.liquidity;
					investorStatistics[cell.getTipoAgente()[0]][6] += cell.investor.investCapital;
					investorStatistics[cell.getTipoAgente()[0]][7] += cell.investor.capitalWithNegativeReturn;
					investorStatistics[cell.getTipoAgente()[0]][8] += cell.investor.sellsAll1;
					investorStatistics[cell.getTipoAgente()[0]][9] += cell.investor.sellsAll0;					
					if(cell.getTipoAgente()[0] == EXPERIMENTED_INVESTOR) {
						investorStatistics[cell.getTipoAgente()[0]][10] += cell.investor.rentabilidadCompra;						
					}
					else if(cell.getTipoAgente()[0] == AMATEUR_INVESTOR)
						investorStatistics[cell.getTipoAgente()[0]][10] += cell.investor.sellTable[0][0];
					if(cell.investor instanceof IntelligentInvestor) {
						IntelligentInvestor intelligentCell = (IntelligentInvestor)cell.investor;
						intelligentStatistics[0][intelligentCell.impulsive?1:0][0] += intelligentCell.buys;
						intelligentStatistics[0][intelligentCell.impulsive?1:0][1] += intelligentCell.sells;
						intelligentStatistics[0][intelligentCell.impulsive?1:0][2]++;
						intelligentStatistics[0][intelligentCell.impulsive?1:0][3] += intelligentCell.getFinancialReputation();
						intelligentStatistics[0][intelligentCell.impulsive?1:0][4] += capital;
						intelligentStatistics[0][intelligentCell.impulsive?1:0][5] += intelligentCell.liquidity;
						intelligentStatistics[0][intelligentCell.impulsive?1:0][6] += intelligentCell.capitalWithNegativeReturn;
						intelligentStatistics[1][intelligentCell.perception?1:0][0] += intelligentCell.buys;
						intelligentStatistics[1][intelligentCell.perception?1:0][1] += intelligentCell.sells;
						intelligentStatistics[1][intelligentCell.perception?1:0][2]++;
						intelligentStatistics[1][intelligentCell.perception?1:0][3] += intelligentCell.getFinancialReputation();
						intelligentStatistics[1][intelligentCell.perception?1:0][4] += capital;
						intelligentStatistics[1][intelligentCell.perception?1:0][5] += intelligentCell.liquidity;
						intelligentStatistics[1][intelligentCell.perception?1:0][6] += intelligentCell.capitalWithNegativeReturn;
						intelligentStatistics[2][intelligentCell.anxiety?1:0][0] += intelligentCell.buys;
						intelligentStatistics[2][intelligentCell.anxiety?1:0][1] += intelligentCell.sells;
						intelligentStatistics[2][intelligentCell.anxiety?1:0][2]++;
						intelligentStatistics[2][intelligentCell.anxiety?1:0][3] += intelligentCell.getFinancialReputation();
						intelligentStatistics[2][intelligentCell.anxiety?1:0][4] += capital;
						intelligentStatistics[2][intelligentCell.anxiety?1:0][5] += intelligentCell.liquidity;
						intelligentStatistics[2][intelligentCell.anxiety?1:0][6] += intelligentCell.capitalWithNegativeReturn;
						intelligentStatistics[3][intelligentCell.memory?1:0][0] += intelligentCell.buys;
						intelligentStatistics[3][intelligentCell.memory?1:0][1] += intelligentCell.sells;
						intelligentStatistics[3][intelligentCell.memory?1:0][2]++;
						intelligentStatistics[3][intelligentCell.memory?1:0][3] += intelligentCell.getFinancialReputation();
						intelligentStatistics[3][intelligentCell.memory?1:0][4] += capital;
						intelligentStatistics[3][intelligentCell.memory?1:0][5] += intelligentCell.liquidity;
						intelligentStatistics[3][intelligentCell.memory?1:0][6] += intelligentCell.capitalWithNegativeReturn;
						intelligentStatistics[4][intelligentCell.isDiversifier?1:0][0] += intelligentCell.buys;
						intelligentStatistics[4][intelligentCell.isDiversifier?1:0][1] += intelligentCell.sells;
						intelligentStatistics[4][intelligentCell.isDiversifier?1:0][2]++;
						intelligentStatistics[4][intelligentCell.isDiversifier?1:0][3] += intelligentCell.getFinancialReputation();
						intelligentStatistics[4][intelligentCell.isDiversifier?1:0][4] += capital;
						intelligentStatistics[4][intelligentCell.isDiversifier?1:0][5] += intelligentCell.liquidity;
						intelligentStatistics[4][intelligentCell.isDiversifier?1:0][6] += intelligentCell.capitalWithNegativeReturn;
					}		
				}
				
				double reputationByAgentType[][][] = new double[2][2][2 + 5];
				long messageStatistics[][] = {{0,0,0,0,0,0,0}, {0,0,0,0,0,0,0}};
				((SimulateSocialExchange)getRoot()).setSortInvestorByActivity(sortByMessageReputation(scape));
				for(int i = 0; i < ((SimulateSocialExchange)getRoot()).getSortInvestorByActivity().size(); i++) {
					Inversores cell = ((SimulateSocialExchange)getRoot()).getSortInvestorByActivity().get(i);
					System.out.print("  id:" + cell.getId() + cell.getAgentTypeToString() + " with messages[");
					int statistics[][] = cell.messageStatistics();
					int historyStatistics[][] = cell.historyMessageStatistics();
					for(int j = 0; j < statistics.length; j++) {
						for(int k = 0; k < statistics[j].length; k++) {
							messageStatistics[j][k] += statistics[j][k] + historyStatistics[j][k];
							System.out.print(statistics[j][k]+"-");
						}
						System.out.print(";");
					}
					System.out.print("],play:" + cell.getPlayed() + ", activ rep:");					
					System.out.print(String.format("%.2f  [", cell.getActivityReputation()));	
					System.out.print(String.format("fri:%.2f,", cell.friendReputation));
					for(int j = 0; j < cell.getMessageReputation().length; j++)
						System.out.print(String.format("mes:%.2f,", cell.getMessageReputation()[j]));
					System.out.println("("+cell.sizeMessageByLimit+")]");
					
					reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][0]++;
					reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][1] += cell.getPlayed();
					reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][2] += cell.getNumMensajes() + cell.getMessagesHistory();
					reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][3] += cell.probabilidadPostear;
					reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][4] += cell.probabilidadBuenMensaje;
					for(int j = 0; j < cell.getMessageReputation().length; j++) {
						reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][j+5] += cell.getMessageReputation()[j];
					}
				}
				printInvestorsStatistics(investorStatistics, intelligentStatistics);
				for(int i = 0; i < reputationByAgentType.length; i++) {
					for(int j = 0; j < reputationByAgentType[i].length; j++) {
						printReputationByAgentType(i,j,reputationByAgentType[i][j]);
					}
				}				
				printMessageStatistics(messageStatistics);
				printIbex35Statistics(((SimulateSocialExchange)getRoot()).getStock());				
				//Clean messages users'history of many time ago
				if(time % Properties.CLEAN_INTERVAL == 1) {
					for(Object cell : scape) {
						if(cell instanceof Inversores) {							
							((Inversores)cell).cleanMessages(time - Properties.MESSAGE_TIME_TO_CLEAN);
						}
					}
				}
				//for(StatCollector stat : ((SimulateSocialExchange)getRoot()).getPeople().getStatCollectors()) {
				//	stat.addValue(randomInRange(0.0,1.0)); //stat.calculateValue());
				//}
			}		
		}		
	}
	
	public void printInvestorsStatistics(double investorStatistics[][], double intelligentStatistics[][][]) {
		System.out.println(" PRU_INV("+investorStatistics[EXPERIMENTED_INVESTOR][2]+"):B:"
				+investorStatistics[EXPERIMENTED_INVESTOR][0]/investorStatistics[EXPERIMENTED_INVESTOR][2]+
				",S:"+investorStatistics[EXPERIMENTED_INVESTOR][1]/investorStatistics[EXPERIMENTED_INVESTOR][2]+
				",Rf:"+investorStatistics[EXPERIMENTED_INVESTOR][3]/investorStatistics[EXPERIMENTED_INVESTOR][2]+
				",Ca:"+investorStatistics[EXPERIMENTED_INVESTOR][4]/investorStatistics[EXPERIMENTED_INVESTOR][2]+
				",La:"+investorStatistics[EXPERIMENTED_INVESTOR][5]/investorStatistics[EXPERIMENTED_INVESTOR][2]+
				",IC:"+investorStatistics[EXPERIMENTED_INVESTOR][6]/investorStatistics[EXPERIMENTED_INVESTOR][2]+
				",CWN:"+investorStatistics[EXPERIMENTED_INVESTOR][7]/investorStatistics[EXPERIMENTED_INVESTOR][2]+
				",RC:"+investorStatistics[EXPERIMENTED_INVESTOR][10]/investorStatistics[EXPERIMENTED_INVESTOR][2]+
				",SA1:"+investorStatistics[EXPERIMENTED_INVESTOR][8]/investorStatistics[EXPERIMENTED_INVESTOR][2]+
				",SA0:"+investorStatistics[EXPERIMENTED_INVESTOR][9]/investorStatistics[EXPERIMENTED_INVESTOR][2]
				);
		System.out.println(" AMA_INV("+investorStatistics[AMATEUR_INVESTOR][2]+"):"+
				investorStatistics[AMATEUR_INVESTOR][0]/investorStatistics[AMATEUR_INVESTOR][2]+
				","+investorStatistics[AMATEUR_INVESTOR][1]/investorStatistics[AMATEUR_INVESTOR][2]+
				","+investorStatistics[AMATEUR_INVESTOR][3]/investorStatistics[AMATEUR_INVESTOR][2]+
				","+investorStatistics[AMATEUR_INVESTOR][4]/investorStatistics[AMATEUR_INVESTOR][2]+
				","+investorStatistics[AMATEUR_INVESTOR][5]/investorStatistics[AMATEUR_INVESTOR][2]+
				","+investorStatistics[AMATEUR_INVESTOR][6]/investorStatistics[AMATEUR_INVESTOR][2]+
				","+investorStatistics[AMATEUR_INVESTOR][7]/investorStatistics[AMATEUR_INVESTOR][2]+
				","+investorStatistics[AMATEUR_INVESTOR][10]/investorStatistics[AMATEUR_INVESTOR][2]+
				","+investorStatistics[AMATEUR_INVESTOR][8]/investorStatistics[AMATEUR_INVESTOR][2]+
				","+investorStatistics[AMATEUR_INVESTOR][9]/investorStatistics[AMATEUR_INVESTOR][2]);
		System.out.println(" RAM_INV("+investorStatistics[RANDOM_INVESTOR][2]+"):"+
				investorStatistics[RANDOM_INVESTOR][0]/investorStatistics[RANDOM_INVESTOR][2]+
				","+investorStatistics[RANDOM_INVESTOR][1]/investorStatistics[RANDOM_INVESTOR][2]+
				","+investorStatistics[RANDOM_INVESTOR][3]/investorStatistics[RANDOM_INVESTOR][2]+
				","+investorStatistics[RANDOM_INVESTOR][4]/investorStatistics[RANDOM_INVESTOR][2]+
				","+investorStatistics[RANDOM_INVESTOR][5]/investorStatistics[RANDOM_INVESTOR][2]+
				","+investorStatistics[RANDOM_INVESTOR][6]/investorStatistics[AMATEUR_INVESTOR][2]+
				","+investorStatistics[RANDOM_INVESTOR][7]/investorStatistics[RANDOM_INVESTOR][2]);
		System.out.println(" IMPULSIVE -> ("+intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][0]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][1]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][3]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][4]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][5]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][6]/intelligentStatistics[0][0][2]+") vs ("+
				intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][0]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][1]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][3]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][4]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][5]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][6]/intelligentStatistics[0][1][2]+")");
		System.out.println(" PERCEPTION -> ("+intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][0]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][1]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][3]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][4]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][5]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][6]/intelligentStatistics[1][0][2]+") vs ("+
				intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][0]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][1]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][3]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][4]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][5]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][6]/intelligentStatistics[1][1][2]+")");
		System.out.println(" ANXIETY -> ("+intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][0]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][1]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][3]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][4]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][5]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][6]/intelligentStatistics[2][0][2]+") vs ("+
			intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][0]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][1]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][3]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][4]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][5]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][6]/intelligentStatistics[2][1][2]+")");
		System.out.println(" MEMORY -> ("+intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][0]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][1]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][3]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][4]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][5]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][6]/intelligentStatistics[3][0][2]+") vs ("+
			intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][0]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][1]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][3]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][4]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][5]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][6]/intelligentStatistics[3][1][2]+")");
		System.out.println(" DIVERSIFIER -> ("+intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][0]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][1]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][3]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][4]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][5]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][6]/intelligentStatistics[4][0][2]+") vs ("+
			intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][0]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][1]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][3]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][4]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][5]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][6]/intelligentStatistics[4][1][2]+")");		
	}
	
	public void printReputationByAgentType (int activityType, int writerType, double reputation[]) {
		String agentType = "[";		
		if(activityType == FREQUENT_USER)
			agentType += "FRE_US";
		else
			agentType += "OCA_US";
		agentType += ":"+String.format("%.3f", reputation[3]/reputation[0])+",";
		if(writerType == GOOD_WRITER)
			agentType += "GOOD_WR";
		else
			agentType += "BAD_WRI";
		agentType += ":"+String.format("%.3f", reputation[4]/reputation[0])+"]";
		System.out.print("  " + agentType + " size:" + reputation[0] + " play:" +
				String.format("%.2f", reputation[1]/reputation[0]) + " mes:" +
				String.format("%.1f", reputation[2]/reputation[0]) + " activity rep:");
		for(int i = 5; i < reputation.length; i++)
			System.out.print(reputation[i]/reputation[0]+",");
		System.out.println();
	}
	
	public void printMessageStatistics(long messageStatistics[][]) {
		String messageString = "  Good Messages:" + messageStatistics[0][0] + ", R:" + 
				messageStatistics[0][1] + ", uniqR:" +	messageStatistics[0][2];
		System.out.println(messageString);
		messageString = "  Bad Messages:" + messageStatistics[1][0] + ", R:" + 
				messageStatistics[1][1] + ", uniqR:" + 	messageStatistics[1][2];
		System.out.println(messageString);
	}
	
	public void printIbex35Statistics(Ibex35 ibex35) {
		for(Share share : ibex35.getAcciones().values()) {
			if(share instanceof RandomShare) {
				RandomShare ramShare = (RandomShare) share;
				System.out.println(ramShare.getName()+": "+ramShare.getValue()+",maxR:"+ramShare.getMaxReached()
						+",minR:"+ramShare.getMinReached()+",vU:"+ramShare.variationUp+",vD:"+
						ramShare.variationDown+","+ramShare.getVariationsHistory());
			} else {
				HistoryFileShare fileShare = (HistoryFileShare) share;
				System.out.println(fileShare.getName()+": "+fileShare.getValue()+","+fileShare.getVariationsHistory());
			}
		}
	}	
	
	public int[][] messageStatistics() {
		int statistics[][] = new int[2][7];
		statistics[0][0] = 0; statistics[0][1] = 0; statistics[0][2] = 0; statistics[0][3] = 0; 
		statistics[0][4] = 0; statistics[0][5] = 0; statistics[0][6] = 0;
		statistics[1][0] = 0; statistics[1][1] = 0; statistics[1][2] = 0; statistics[1][3] = 0; 
		statistics[1][4] = 0; statistics[1][5] = 0; statistics[1][6] = 0;
		for(Message message : getMensajes()) {
			if((time-message.getDate()) < Properties.TIME_LIMIT) {
				if(message.isGood()) {
					statistics[0][0]++;
					statistics[0][1] += message.getNumReaders();
					statistics[0][2] += message.getUniqueNumReaders();
					statistics[0][3] += message.getNumFollowers();
					statistics[0][4] += message.getUniqueNumFollowers();
					statistics[0][5] += message.getScores().size();
					statistics[0][6] += message.getScore();
				} else {
					statistics[1][0]++;
					statistics[1][1] += message.getNumReaders();
					statistics[1][2] += message.getUniqueNumReaders();
					statistics[1][3] += message.getNumFollowers();
					statistics[1][4] += message.getUniqueNumFollowers();
					statistics[1][5] += message.getScores().size();
					statistics[1][6] += message.getScore();
				}
			}
		}
		return statistics;
	}
	
	public int[][] historyMessageStatistics() {
		int statistics[][] = {{messageHistory[0],readerHistory[0],uniqueReaderHistory[0],
			followerHistory[0],uniqueFollowerHistory[0],scorerHistory[0],scoreHistory[0]}, 
			{messageHistory[1],readerHistory[1],uniqueReaderHistory[1],followerHistory[1],
			uniqueFollowerHistory[1],scorerHistory[1],scoreHistory[1]}};
		return statistics;
	}
	
	private void cleanMessages (int timeLimit) {
		for(int i = 0; i < misMensajes.size(); i++) {
			Message message = misMensajes.get(i);
			if(message.getDate() < timeLimit) {
				if(message.isGood()) {
					messageHistory[0]++;
					readerHistory[0] += message.getNumReaders();
					uniqueReaderHistory[0] += message.getUniqueNumReaders();
					followerHistory[0] += message.getNumReaders();
					uniqueFollowerHistory[0] += message.getUniqueNumFollowers();
					scorerHistory[0] += message.getScores().size();
					scoreHistory[0] += message.getScore();
				} else {
					messageHistory[1]++;
					readerHistory[1] += message.getNumReaders();
					uniqueReaderHistory[1] += message.getUniqueNumReaders();
					followerHistory[1] += message.getNumReaders();
					uniqueFollowerHistory[1] += message.getUniqueNumFollowers();
					scorerHistory[1] += message.getScores().size();
					scoreHistory[1] += message.getScore();
				}
				misMensajes.remove(i);				
				i--;
			}
			else
				return;
		}
	}
	
	private List<Inversores> sortByMessageReputation(Scape agentsList) {
		List<Inversores> sortList = new ArrayList<Inversores>();
		boolean sorted;
		for (Object cell : agentsList) {
			if(cell instanceof Inversores) {
				sorted = false;
				for(int i = 0; i < sortList.size(); i++) {
					if(((Inversores)cell).getMessageReputation()[0] 
					        >= sortList.get(i).getMessageReputation()[0]) {
						sortList.add(i, (Inversores)cell);
						sorted=true;
						break;
					}
				}
				if(!sorted)
					sortList.add((Inversores)cell);
			}
		}
		return sortList;
	}
	
	private List<Inversores> sortByFinancialReputation(Scape agentsList) {
		List<Inversores> sortList = new ArrayList<Inversores>();
		boolean sorted;
		for (Object cell : agentsList) {
			if(cell instanceof Inversores) {
				sorted = false;
				for(int i = 0; i < sortList.size(); i++) {
					if(((Inversores)cell).investor.getFinancialReputation() >= 
							sortList.get(i).investor.getFinancialReputation()) {
						sortList.add(i, (Inversores)cell);
						sorted=true;
						break;
					}
				}
				if(!sorted)
					sortList.add((Inversores)cell);
			}
		}
		return sortList;
	}
	
	public void update(){
		iteraciones++;
		isNewIteration = true;
		//post in own page.
		if(randomInRange(0,1.0) < probabilidadPostear){
			postear();
		}
		//update thresholds
	}
	
	@SuppressWarnings("unchecked")
	public void chooseNeighborToPlay() {
		List<Inversores> neighbors = findWithin(Properties.NEIGHBOR_DISTANCE_TO_PLAY);
		for(Inversores neighbor : neighbors) {
			double probabilityToPlay = Math.pow(Properties.NEIGHBOR_DISTANCE_EXPONENCIAL_DEGRADATION,
					calculateDistance(neighbor)) * probabilidadLeer;
			if(randomInRange(0.0,1.0) < probabilityToPlay)
				play(neighbor);
			//System.out.println("("+ time +") id :" + getId() + ",pob:" + probabilityToPlay + " cal:" + calculateDistance(neighbor));
		}
		//System.out.println("("+ time +") id :" + getId() + ", played:" + played + " " + neighbors.size());
	}	
	
	/**
	 *  Interactuamos con la bolsa
	 * @param miBolsa
	 */
	public void jugarEnBolsa(Ibex35 myStock){
		//posiblidad de si entra en vender, la probabilidad de entrar en comprar sea menor 
		//y al reves		
		investor.jugarEnBolsa(myStock);	
	}	
	
	public void generateActivityReputation () {
		for(int i = 0; i < messageReputation.length/2; i++)
			messageReputation[i] = 0;
		int timeLimit = Properties.MAX_DIFFERENCE_CLUSTERS * Properties.TIME_CLUSTER;
		sizeMessageByLimit = 0;
		for (int j = misMensajes.size()-1; j >= 0; j--) { //Message message : misMensajes ) {
			Message message = misMensajes.get(j);	
			int timeDifference = time - message.getDate();
			if(timeDifference >= timeLimit) {
				break;
			}
			sizeMessageByLimit++;
			for(int i = 0; i < message.getReputation().length; i++) {
				int clusterDifference = timeDifference / Properties.TIME_CLUSTER;
				messageReputation[i] += message.getReputation()[i] 
				        * Math.pow(Properties.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR, -clusterDifference);
				//LINEAL: messageReputation[i] += mensaje.getReputation()[i] / clusterDifference;
			}
		}
		for(int i = 0; i < messageReputation.length/2; i++)
			messageReputation[i+messageReputation.length/2] = messageReputation[i];
		if(sizeMessageByLimit > Properties.MINIMUM_MESSAGES_TO_DEGRADATE) {
			if(Properties.MESSAGE_DEGRADATION_LOGARITHMIC_FACTOR == Math.E) {
				for(int i = 0; i < messageReputation.length/2; i++) {
					messageReputation[i] /= 
							Math.log(((double)sizeMessageByLimit)/Properties.MINIMUM_MESSAGES_TO_DEGRADATE*Math.E);					
				}				
			} else {
				for(int i = 0; i < messageReputation.length/2; i++) {
					messageReputation[i] /= Math.log10(sizeMessageByLimit/Properties.MINIMUM_MESSAGES_TO_DEGRADATE*10);
				}				
			}
		}
		//friendReputation
		int commonFriends = 0;
		for(Inversores friend: friends) {
			if(followers.contains(friend))
				commonFriends++;
		}
		if(commonFriends == 0) {
			if(followers.size()-commonFriends > 0) {
				friendReputation = Properties.NO_COMMON_FRIEND_WEIGHT * Math.log(followers.size()-commonFriends);
			}
		} else {
			if(followers.size()-commonFriends > 0) {
				friendReputation = Properties.NO_COMMON_FRIEND_WEIGHT * Math.log(followers.size()-commonFriends) 
					+ Properties.COMMON_FRIEND_WEIGHT * Math.log(commonFriends);
			} else {
				friendReputation = Properties.COMMON_FRIEND_WEIGHT * Math.log(commonFriends);
			}			
		}
					
	}
	
	public double getActivityReputation () {
		return Properties.MESSAGE_WEIGHT * messageReputation[1] + Properties.FRIEND_WEIGHT * friendReputation;
	}
		
	public double[] getMessageReputation () {
		return messageReputation;
	}	
	
	/**
	 * Comentamos un post de otro inversor si tiene. Esto ocurre para los vecinos 
	 * @param agent
	 */
	public void play(Agent partner){
		boolean isFirst = true;		
		//Friend Relationship		
		if(randomInRange(0.0,1.0) < friendlyProbability && !friends.contains(partner)) {			
			if(randomInRange(0.0,1.0) < getFriendlyProbability((Inversores) partner)) {
				friends.add((Inversores)partner);
				((Inversores) partner).followers.add(this);
			}			
		}	
		//TODO: probabilidad de leer por cronologia y el resto de leer por reputacion total (mensajes recomendados / mensajes cronologicos)
		//Leer mensaje con mas reputacion (por cronologia y/o reputacion total)
		// las dem�s lecturas ir bajando probabilidad de leer -> Probabilidad_leer_recomendados/cronologicos
		//TODO: afinidad de agentes, si un agente lee mucho de otro se le aumenta su afinidad de forma que juegue con de forma
		//  m�s probable
		
		//By cronology:		 
		ArrayList<Message> messagesFromPartner = ((Inversores) partner).getMensajes();			
		double consecutiveMessageDegradation = 1.0;
		int notReadMessages = 0;
		for (int id = messagesFromPartner.size()-1; id >= 0; id--){
			Message message = messagesFromPartner.get(id);
			double probabilities[] = getReadCommentAndScoreProbability(message, isFirst);
			if(probabilities == null) //
				return;
			if(randomInRange(0, 1.0) < (probabilities[0] * consecutiveMessageDegradation) ) {
				played++;
				isFirst = false;
				message.addReader(this);
				//Score
				if(randomInRange(0.0,1.0) < probabilities[2]) {
					if(message.isGood()) {
						message.addScore(this, randomInRange(
								Properties.GOOD_MESSAGES_SCORE_PROBABILITY[0],
								Properties.GOOD_MESSAGES_SCORE_PROBABILITY[1]));
					} else {
						message.addScore(this, randomInRange(
								Properties.BAD_MESSAGES_SCORE_PROBABILITY[0],
								Properties.BAD_MESSAGES_SCORE_PROBABILITY[1]));
					}					
				}
				//System.out.println("("+ time +") id: " + this.getId() + " lee mensaje("+probabilidadLeer +","+
				//		probabilities[0]+") "+id + "("+ mensaje.getPopularity() + ") del inversor "+((Inversores) partner).getId());
				//Comment:
				if(randomInRange(0, 1.0) < probabilities[1]) {
					message.addComment(this);
					//System.out.println("("+ time +") id: " + this.getId() + " comenta mensaje("+probabilidadComentar +","+
					//		probabilities[1]+") "+id + " del inversor "+((Inversores) partner).getId());
				}
			} else if(++notReadMessages >= Properties.NOT_READ_MESSAGES_TO_LEAVE_USER)
				return;
			consecutiveMessageDegradation *= Properties.CONSECUTIVE_MESSAGE_CRONOLOGY_READ_DEGRADATION;
		}	
		
		
		/*
		//By recommendation
		int previousPosition = -1;
		while (  (previousPosition = 
				getNextMessageByOwner( ((SimulateSocialExchange)getRoot()).getPopularMessages(), this, previousPosition) ) != -1  ){
			Message message = ((SimulateSocialExchange)getRoot()).getPopularMessages().get(previousPosition);
			double probabilities[] = getReadAndCommentProbability(message);
			if(probabilities == null) //
				return;
			if(isFirst || randomInRange(0, 1.0) < (probabilities[0] * consecutiveMessageDegradation) ) {
				played++;
				isFirst = false;
				message.addReader(this);
				//System.out.println("("+ time +") id: " + this.getId() + " lee mensaje("+probabilidadLeer +","+
				//		probabilities[0]+") "+id + "("+ mensaje.getPopularity() + ") del inversor "+((Inversores) partner).getId());						
				if(randomInRange(0, 1.0) < probabilities[1]) {
					message.addComment(this);
					//System.out.println("("+ time +") id: " + this.getId() + " comenta mensaje("+probabilidadComentar +","+
					//		probabilities[1]+") "+id + " del inversor "+((Inversores) partner).getId());
				}
			} else
				return;
			consecutiveMessageDegradation *= Properties.CONSECUTIVE_MESSAGE_RECOMMENDATION_READ_DEGRADATION;
		}
		*/
	}
	
	public double getFriendlyProbability(Inversores partner) {
		double probability = 1.5;
		double afinity = 0;
		ArrayList<Message> messagesFromPartner = partner.getMensajes();
		for(int i = messagesFromPartner.size()-1; i >= 0; i--) {
			Message message = messagesFromPartner.get(i);
			if((time - message.getDate()) > Properties.TIME_LIMIT)
				break;
			afinity += message.getUniqueReaders().contains(this)?1:0  +
					(message.getUniqueFollowers().contains(this)?1:0) * 3 + 
					(message.getScores().containsKey(this)?message.getScores().get(this):0);
		}
		double afinityAverage = 1;
		for(Inversores friend : friends) {
			ArrayList<Message> messagesFromFriend = friend.getMensajes();
			for(int i = messagesFromFriend.size()-1; i >= 0; i--) {
				Message message = messagesFromFriend.get(i);
				if((time - message.getDate()) > Properties.TIME_LIMIT)
					break;
				afinityAverage += message.getUniqueReaders().contains(this)?1:0  +
						(message.getUniqueFollowers().contains(this)?1:0) * 3 + 
						(message.getScores().containsKey(this)?message.getScores().get(this):0);
			}
		}
		if(friends.size() > 0) {
			afinityAverage /= friends.size();		
			probability *= afinity / afinityAverage;
			int commonFriends = 1;
			for(Inversores friend : friends) {
				if(partner.getFriends().contains(friend)) 
					commonFriends++;			
			}
			if(commonFriends > 1/Properties.FRIENDLY_COMMON)
				probability *= Properties.FRIENDLY_COMMON * commonFriends;
			//else
			//	probability *= Properties.FRIENDLY_COMMON;
		}		
		if(friends.size() > Properties.FRIEND_DEGRADATION) {
			probability *= Math.pow(((double)friends.size())/Properties.FRIEND_DEGRADATION, 
					Properties.EXPONENCIAL_FRIEND_STRENGH_DECREMENT);
		}
		return probability;
	}
	
	public int getNextMessageByOwner (List<Message> messages, Inversores owner, int previousPosition) {
		for(int i = previousPosition + 1; i < messages.size(); i++) {
			Message message = messages.get(i);
			if(message.getIdOwner() == owner.getId())
				return i;
		}
		return -1;
	}
	
	/**
	 * - time difference: clustering the time difference in intervals of TIME_CLUSTER
	 *      and applying cronology degradation (negative exponencial with factor: CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR)
	 *      with a maximum iteration difference (MAX_DIFFERENCE_CLUSTERS)
	 *  - bad message degradation indicating by BAD_MESSAGE_DEGRADATION
	 *  
	 * Read probability:
	 *  - cronology degradation, bad message degradation and popularity (its position into the popularMessages list)
	 *  - if user has not commented the message:
	 *     - if user has already read the message: only read the message if it is good and with 
	 *        a degradation: ALREADY_READ_MESSAGE
	 *     - if user has not read the message: normal probability 
	 *  - if user has commented the message:
	 *     - and new comments have been appended to the message: normal probability
	 *     - no comments have been appended to the message: read the message with a degradation: ALREADY_COMMENTED_MESSAGE
	 *     
	 * Comment probability:
	 *  - cronology degradation, bad message degradation and read probability
	 *  - if user has not commented the message: normal probability
	 *  - if user has already commented the message: degradation of ALREADY_COMMENTED_MESSAGE
	 *  
	 */
	private double[] getReadCommentAndScoreProbability (Message message, boolean isFirst) {
		double probabilities[] = new double[] {0,0,0};
		int time_difference = (time - message.getDate()) / Properties.TIME_CLUSTER;
		if(time_difference > Properties.MAX_DIFFERENCE_CLUSTERS)
			return null;
		double cronology_degradation = 
			Math.pow(Properties.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR,-time_difference);
		double bad_message_degradation = message.isGood() ? 1 : Properties.BAD_MESSAGE_DEGRADATION;
		int followersPosition = message.getFollowers().lastIndexOf(this);
		if(followersPosition == -1) {
			if(message.getReaders().contains(this)) {
				if(message.isGood())
					probabilities[0] = (isFirst ? 1.0 : cronology_degradation * probabilidadLeer)
						* Properties.ALREADY_READ_MESSAGE * message.getPopularity()[0];
			}
			else
				probabilities[0] = (isFirst ? 1.0 : cronology_degradation * probabilidadLeer)
					* bad_message_degradation * message.getPopularity()[0];			
		}
		else if (followersPosition != message.getFollowers().size()-1)
			probabilities[0] = (isFirst ? 1.0 : cronology_degradation * probabilidadLeer)
				* bad_message_degradation * message.getPopularity()[0];
		else
			probabilities[0] = (isFirst ? 1.0 : cronology_degradation * probabilidadLeer)
				* bad_message_degradation * Properties.ALREADY_COMMENTED_MESSAGE * message.getPopularity()[0];
		if(followersPosition == -1)
			probabilities[1] = cronology_degradation * probabilidadComentar;
		else
			probabilities[1] = cronology_degradation * probabilidadComentar * Properties.ALREADY_COMMENTED_MESSAGE;
		if(message.getScores().containsKey(this))
			probabilities[2] = cronology_degradation * scoreProbability * Properties.ALREADY_SCORED_MESSAGE;
		else
			probabilities[2] = cronology_degradation * scoreProbability;
		return probabilities;
	}
	
	
	/**
	 * Post a message
	 */
	public void postear(){
	  //System.out.println("("+ time +") id: " + this.getId() + " posteo");
	  misMensajes.add(new Message(this, time, (randomInRange(0,1.0) < probabilidadBuenMensaje)? true : false));
	}
	
	/**
	 * Return us the list of written messages
	 */
	public ArrayList<Message> getMensajes(){
		return misMensajes;
	}
	
	/*
	 * Hace falta un metodo que devuelva todos tus seguidores (unicos y repetidos)
	 * y empezar a valorar
	 * 
	 * tb que inviertan de alguna manera
	 */
	public int getNumMensajes(){
		return misMensajes.size();
	}
	
	
	public int getReaders(){
		int total = 0;
		for(Message message : misMensajes){
			total += message.getNumReaders();
		}
		return total;
	}
	
	/*public int getReadersUnicos(){
		HashSet<Inversores> uniqueReaders = new HashSet<Inversores>();
		for(Message message : misMensajes){
			HashSet<Inversores> followers1Comment = message.getUniqueReaders();
			for(Inversores investor : followers1Comment){
				uniqueReaders.add(investor);
			}
		}
		return uniqueReaders.size();
	}*/
	
	/**
	 * Ask for the all of followers of whole comments that Investor has. Doesn't care
	 * if the investors has repeated.
	 * 
	 * @return The number of people that have responded at one o more comments of one investor 
	 */
	public int getFollowers(){
		int suma = 0;
		for(Message message : misMensajes){
			suma += message.getNumFollowers();
		}
		return suma;
	}	
	
	/**
	 * Ask for the singles followers of a Investor	 
	 * @return The number of single investors had responded you in a comment 
	 */
	/*public int getFollowersUnicos(){
		HashSet<Inversores> followersUnicos = new HashSet<Inversores>();
		for(Message mensaje : misMensajes){
			HashSet<Inversores> followers1Comment = mensaje.getUniqueFollowers();
			for(Inversores inversor : followers1Comment){
				followersUnicos.add(inversor);
			}
		}
		return followersUnicos.size();
	}*/
	
	/**
	 * Getter of Color
	 * @param ncolor
	 */
	//hay que llamalos setColor y getColor para que ascape los pinte
	public void setColor(Color ncolor) {
		myColor = ncolor;
	}
	/**
	 * Setter of Color
	 */
	public Color getColor() {
		return myColor;
	}
	
	/**
	 * Getter of Id investor
	 * @return id
	 */
	public int getId() {
		return id;
	}
	/**
	 * Setter of Id investor
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	public int getTime() {
		return time;
	}	

	public InvestorType getInvestor() {
		return investor;
	}
	
	private int getMessagesHistory () {
		int messagesNum = 0;
		for(int messages : messageHistory)
			messagesNum += messages;
		return messagesNum;
	}
	
	public int[] getTipoAgente() {
		return tipoAgente;
	}
	
	public int getPlayed () {
		return played;
	}
	public HashSet<Inversores> getFriends () {
		return friends;
	}
	
	public void setTipoAgente(int[] tipoAgente) {
		this.tipoAgente = tipoAgente;
	}
		
	public String getMessageHistory() {
		String messagesHistory = "";
		int goodBadCount[] = new int[]{0,0};
		for(Message message : misMensajes) {
			if(message.isGood())
				goodBadCount[0]++;
			else
				goodBadCount[1]++;
		}
		for(int i = 0; i < messageHistory.length; i++) {
			messagesHistory += (messageHistory[i] + goodBadCount[i])+",";
		}
		return messagesHistory;
	}
	
	public String getAgentTypeToString () {
		String agentType = "[";
		if(tipoAgente[0] == EXPERIMENTED_INVESTOR)
			agentType += "PRU_IN,";
		else if(tipoAgente[0] == AMATEUR_INVESTOR)
			agentType += "AMA_IN,";
		else
			agentType += "RAM_IN,";
		if(tipoAgente[1] == FREQUENT_USER)
			agentType += "FRE_US";
		else
			agentType += "OCA_US,";
		agentType += ":"+String.format("%.2f", probabilidadPostear)+",";
		if(tipoAgente[2] == GOOD_WRITER)
			agentType += "GOOD_W,";
		else
			agentType += "BAD_WR";
		agentType += ":"+String.format("%.2f", probabilidadBuenMensaje);
		if(tipoAgente[3] == FRIENDLY_USER)
			agentType += ",FRI";
		else
			agentType += ",NOF";
		return agentType + ":"+friends.size()+String.format("-%.2f",friendlyProbability) 
				+ "]" + investor.getAgentTypeToString();
	}
}