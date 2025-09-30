/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataStruct;

/**
 *
 * @author vivia
 */
public class Cola {
    private Nodo pLast;
    private Nodo pFirst;
    private int tamano;

    public Cola() {
        this.pLast = null;
        this.pFirst = null;
        this.tamano = 0;
    } 

    public Nodo getpLast() {
        return pLast;
    }

    public Nodo getpFirst() {
        return pFirst;
    }

    public int getTamano() {
        return tamano;
    }

    public void setpLast(Nodo pLast) {
        this.pLast = pLast;
    }

    public void setpFirst(Nodo pFirst) {
        this.pFirst = pFirst;   
    }
    
    public void setTamano(int tamano) {
        this.tamano = tamano;
    }
    
    public boolean EsVacio() {
        return pFirst == null;
    }

    
    public void addNumero(int numero) { // quiz
        String strNum = Integer.toString(numero);
        for (int i = 0; i < strNum.length(); i++) {                  
            char num = strNum.charAt(i);
            Nodo nuevoNodo = new Nodo(num); 
            if (EsVacio()) {
                pFirst = nuevoNodo;
                pLast = nuevoNodo;  
            }
            else {
                Nodo aux = pLast;
                aux.setNext(nuevoNodo);
                pLast = nuevoNodo;           
            } 
            tamano += 1;         
        }
    }
    
    public Object ObtenerElemento(int posicion) {
        if (posicion < 0 || posicion >= tamano) {
            System.out.println("La posición debe estar entre 0 y " + (tamano - 1));
        }
         
        Nodo actualNodo = pFirst;
        
        for (int i = 0; i < posicion; i++) {
            actualNodo = actualNodo.getNext();
        }
        return actualNodo.getInfo();
    }
    
    public void InsertarFinal(Object info) {
        Nodo nuevoNodo = new Nodo(info); 
        if (EsVacio()) {
            pFirst = nuevoNodo;
            pLast = nuevoNodo;  
        }
        else {
            Nodo aux = pLast;
            aux.setNext(nuevoNodo);
            pLast = nuevoNodo;           
        } 
        tamano += 1;
    }
    
    public void Imprimir(){
        if(!EsVacio()){
            Nodo aux= pFirst;
            for (int i = 0; i < tamano; i++) {
                System.out.println(aux.getInfo()+ " ");
                aux= aux.getNext();
            }
        } else {
            System.out.println("La lista esta vacia.");
        }
    }
    
    public String RetornarLista(){
        String cadena="";
        if (!EsVacio()) {
            Nodo aux= pFirst;
            for (int i = 0; i < tamano; i++) {
                cadena = cadena + aux.getInfo() + "\n"; // acomodar, poner comas o saltos de linea.
                aux= aux.getNext();               
            }
        }
        return cadena;          
    }
    
    public void EliminarInicio(){
        if(!EsVacio()){
            pFirst= pFirst.getNext();
            tamano= tamano-1;
        }
    }
    
    public void EliminarFinal(){
        if(!EsVacio()){
            pLast= pLast.getNext();
            tamano= tamano-1;
        }
    }
    
    public void Leer (Nodo nodo){
        System.out.println(nodo.getInfo());
    }
    
    public void VoltearLista() {
      if (EsVacio()){
          return;          
      } 
      
      Nodo actual = pFirst;
      Nodo anterior = null;
      Nodo siguiente;
     
      
      while (actual != null) {
          siguiente = actual.getNext();
          actual.setNext(anterior);
          anterior = actual;
          actual = siguiente;
      }
      pFirst = anterior;
    }
    
    public Nodo Buscar(Object x) {
        boolean encontrado = false;
        Nodo P = null;

        if (!EsVacio()) {
            P = pFirst;
            while (!encontrado && P != null) {
                if (P.getInfo().equals(x)) {
                    encontrado = true;
                } else {
                    P = P.getNext();
                }
            }
        }
        return P;
    }
    
    public void InsertarPrimero(Object info) {
        Nodo nuevoNodo = new Nodo(info);
        
        if (!EsVacio()) {
            Nodo aux = pFirst;
            nuevoNodo.setNext(aux);
            pFirst = nuevoNodo;
        }else {
           pFirst = nuevoNodo;
           pLast = nuevoNodo;
        }       
        tamano += 1;
    }
    
    public void EliminarNodo(Object info) {
        if (EsVacio()) {
            return;
        }
        Nodo actual = pFirst;
        Nodo anterior = null;
        
        while (actual != null) { // Esto porque cuando terminamos de recorrer la lista, el ultimo apunta a null.            
            if (actual.getInfo() == info) {
                if (anterior == null) {
                    pFirst = actual.getNext();
                    if (pFirst == null) {
                        pLast = null;
                    }
                } else {
                    anterior.setNext(actual.getNext());
                    if (actual == pLast) {
                        pLast = anterior;
                    }
                }
                tamano = tamano - 1;
                break;
            }
            anterior = actual;
            actual = actual.getNext();
        }
        System.out.println("No se encontro el elemento en la lista");
    }
    
    public void EliminarLista() {
        while (pFirst != null) {
            pFirst = pFirst.getNext();
        }
    }
    
    public void Invertir() {
        Nodo aux;
        Nodo pNew;
        
        aux = pFirst;
        while (aux != null) {
            Nodo nuevo = new Nodo(aux.getInfo());
            
        }
    }
    
    public void ImprimirNodo(Nodo nodo){
        if(!EsVacio()){
            Nodo aux= nodo;
            System.out.println(aux.getInfo()+ " ");
            
        } else {
            System.out.println("La lista esta vacia.");
        }
    }
}
