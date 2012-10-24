import scala.io._
import java.io.PrintWriter
import javax.swing.text.NumberFormatter
import java.text.DecimalFormat
import java.text.NumberFormat
object ConvertCSV {
  def  main (args:Array[String] ){
    if (args.length!=2)
      throw new Exception("Got "+args.length+ " arguments. Enter correct num of Arguments. Usage ConvertCSV inputfile.csv correctedfile.csv")
    
    val origFile = args(0)
    println ("You are opening from " + origFile  )
    val outFile = args(1)
    val writer= new PrintWriter(outFile)
    var lineCount:Int=1
    for (line <- Source.fromFile(origFile).getLines()){
      writer.println(formatLine(line,lineCount))
      lineCount+=1
    }
    writer.close()   
  }
  
  def formatLine(currLine:String,lineCount:Int):String={
    val tokens:Array[String] = currLine.replace(", ,", ",").split("(,|\",)")
    var newString:String=""
    if (!(tokens.length == 14 )){
      println("In line " + lineCount + "found " + tokens.length + " tokens" )
      return newString
    }
    //val formatter:NumberFormat = new DecimalFormat("#.############")
    var tokenCount:Int=0
    for (token <- tokens){
      val newTok:String = tokenCount match{
        case 0 => token
        case 1 => token
        case 2 => "\""+ token + "\""
        case 3 => token
        case 4 => token
        case 5 => token 
        case 6 => token
        case 7 => token
        case 8 => token
        case 9 => token
        case 10 => token
        case 11 => token
        case 12 => token
        case 13 => token
        case 14 => token
        
      }
      tokenCount+=1
      newString+= newTok.trim()+","
    }
    newString.substring(0, newString.length-1)
    
      
      
  }
  

}