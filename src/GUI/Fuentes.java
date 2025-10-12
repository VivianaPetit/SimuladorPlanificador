/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import java.awt.Font;
import java.io.InputStream;
/**
 *
 * @author VivianaPetit
 */
public class Fuentes {
    private Font font = null;
    public String nombre = "Arial";
    

    /* Font.PLAIN = 0 , Font.BOLD = 1 , Font.ITALIC = 2
 * tamanio = float
 */
    public Font fuente(String fontName, int estilo, float tamanio)
    {
         try {
            //Se carga la fuente
            InputStream is =  getClass().getResourceAsStream(fontName);
            font = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            //Si existe un error se carga fuente por defecto ARIAL
            font = new Font("Roboto", Font.PLAIN, 8);            
        }
        Font tfont = font.deriveFont(estilo, tamanio);
        return tfont;
    }
}