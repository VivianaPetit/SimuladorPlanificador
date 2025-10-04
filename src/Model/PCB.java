/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author Jose
 */
public class PCB {
    private int pid;
    private String name;
    private int totalInstructions ;
    private int remainingInstructions ;
    private int pc ;
    private int mar ;
    private int memoryNeeded;
    private boolean cpuBound ;
    private int cyclesToException  ;
    private int exceptionServiceCycles;
    private int priority ;
    
    public enum Status {
        NEW, READY, RUNNING, BLOCKED, SUSPENDED, TERMINATED
    }
     
   
    private int arrivalTime;
    private int startTime;
    private int finishTime;
    
    private Status status;
    
     public PCB(int pid, String name, int totalInstructions, boolean cpuBound,
               int cyclesToException, int exceptionServiceCycles,
               int memoryNeeded, int priority, int arrivalTime) {
         
        this.pid = pid;
        this.name = name;
        this.totalInstructions = totalInstructions;
        this.remainingInstructions = totalInstructions;
        this.pc = 0;
        this.mar = 0;
        this.cpuBound = cpuBound;
        this.cyclesToException = cyclesToException;
        this.exceptionServiceCycles = exceptionServiceCycles;
        this.memoryNeeded = memoryNeeded;
        this.priority = priority;
        this.arrivalTime = arrivalTime;

        this.startTime = -1;   // aún no ha iniciado
        this.finishTime = -1;  // aún no ha terminado
        this.status = Status.NEW;
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public int getTotalInstructions() {
        return totalInstructions;
    }

    public int getMemoryNeeded() {
        return memoryNeeded;
    }

    public boolean isCpuBound() {
        return cpuBound;
    }

    public int getExceptionServiceCycles() {
        return exceptionServiceCycles;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setRemainingInstructions(int remainingInstructions) {
        this.remainingInstructions = remainingInstructions;
    }

    public int getRemainingInstructions() {
        return remainingInstructions;
    }

    public int getPc() {
        return pc;
    }
    
    public void setPc(int pc) {
        this.pc = pc;
    }

    public int getMar() {
        return mar;
    }
    
    public void setMar(int mar) {
        this.mar = mar;
    }

    public int getStartTime() {
        return startTime;
    }
    
     public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }
    
    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCyclesToException(int cyclesToException) {
        this.cyclesToException = cyclesToException;
    }
    
    public int getCyclesToException() {
        return cyclesToException;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public int getPriority() {
        return priority;
    }

   

    

    
    
}
