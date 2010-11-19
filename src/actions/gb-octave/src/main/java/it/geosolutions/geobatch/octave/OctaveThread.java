package it.geosolutions.geobatch.octave;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OctaveThread implements Runnable {
    
    private final static Logger LOGGER = Logger.getLogger(OctaveThread.class.toString());
    
    /**
     * Octave Environment
     */
    private OctaveEnv<?> env;
    
    /**
     * Octave Engine
     */
    private Engine engine;
    
    /**
     * Constructor
     * @param e environment
     */
    public OctaveThread(OctaveEnv<?> e){
        env=e;
        engine=new Engine();
    }

    public void run() {
        String comm="";
        /**
         * Objects are extracted from the list
         * since each returning value should be returned
         * to the requesting process using an XML message
         */
        while (comm!="quit"){
            while (!env.hasNext()){
        //TODO: check is clean comm needed?
                // formally we have to clean last command line
                comm="";
                synchronized (env) {
                    try {
                        env.notifyAll();
                        env.wait();
                    } 
                    catch (InterruptedException e) {
                        if (LOGGER.isLoggable(Level.WARNING))
                            LOGGER.warning(e.getMessage());
                    }
                }
            }
            // extract next ExecutableSheet
            OctaveExecutableSheet sheet=env.pop();
            if (sheet!=null){
                while (sheet.hasCommands()){
                    // extract
                    comm=sheet.popCommand();
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("Octave extracting new command to execute...");
                    // put definitions into octave environment
                    if (sheet.hasDefinitions())
                        engine.put(sheet.getDefinitions());
                    // evaluate commands (f.e.: source files)
                    if (comm!=null && comm!="" && comm!="quit"){                        
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER.info("Octave process is running command->"+comm+"<-");
                        
                        // evaluate command  
                        engine.eval(comm);
                        
//                      synchronized (sheet) {
//                         
//                              try {
//                                    sheet.wait(1);
//                                    System.out.println("RUNNING_OCTAVE");
//                                    
//                                } catch (InterruptedException e) {
//                                    LOGGER.log(Level.FINER, e.getMessage(), e);
//                                }
//                                
//                        }
                    }
                    else {
                        if (LOGGER.isLoggable(Level.WARNING))
                            LOGGER.warning("Unable to run an empty command!");
                    }
        
                    // sheet returns becomes global returns
                    if (sheet.hasReturns())
                        env.global.pushReturns(sheet.getReturns());
                    
                    // clear sheet environment
                    if (sheet.hasDefinitions())
                        engine.clear(sheet.getDefinitions());
                
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("Octave extracting a new OctaveExecutableSheet");

//TODO
//                sheet.gate.countDown();
//                    
//              synchronized (sheet.gate) {
//                        sheet.gate.notifyAll();
//                    }
//                
                }
            }
            synchronized (env) {
                env.notifyAll();
            }
            
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Octave process exiting");
            
        } // comm!="quit"
        // closing Octave engine
        engine.close();
    }

}