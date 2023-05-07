package com.bpitindia.timetable.model

import android.content.Context
import android.os.Environment
import android.util.Log
import com.bpitindia.timetable.utils.DbHelper
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream


class DownloadExcel {

    private lateinit var workbook: HSSFWorkbook
    private lateinit var sheet: HSSFSheet
    private lateinit var db: DbHelper

    private val days = arrayListOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    private val periods = arrayListOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII")


    fun createExcelWorkbook(context: Context) {

        Log.e("asd", "finally149")

        db = DbHelper(context)
        val monday = db.getWeek("Monday")
        val tuesday = db.getWeek("Tuesday")
        val wednesday = db.getWeek("Wednesday")
        val thursday = db.getWeek("Thursday")
        val friday = db.getWeek("Friday")

        //new workbook
        workbook = HSSFWorkbook()


        sheet = workbook.createSheet("TimeTable")
//        val cellStyle = CellStyler().createWarningColor(workbook)

//        val cellStyle = workbook.createCellStyle()
//        cellStyle.wrapText = true
//        cellStyle.shrinkToFit = true




        val row0 = sheet.createRow(0)
        val r0c0 = row0.createCell(0)

        r0c0.setCellValue("!!BHAGWAN PARSHURAM INSTITUTE OF TECHNOLOGY!!")
//        r0c0.setCellStyle(cellStyle)

        val row2 = sheet.createRow(2)
        val r2c0 = row2.createCell(0)


//        r2c0.setCellStyle(cellStyle)
        r2c0.setCellValue("Period → \n Day ↓")

        //days
        for(i in 3..7){
            val row = sheet.createRow(i)
            val cell = row.createCell(0)
//            cell.setCellStyle(cellStyle)
            cell.setCellValue(days[i-3])
        }

        //periods
        for(i in 1..8){
            val cell = row2.createCell(i)
//            cell.setCellStyle(cellStyle)
            cell.setCellValue(periods[i-1])
        }

        //monday
        val row3 = sheet.getRow(3)
        for(i in 1..monday.size){
            val cell = row3.createCell(i)
//            cell.setCellStyle(cellStyle)
            cell.setCellValue("${monday[i-1].subject}\n${monday[i-1].teacher}\n${monday[i-1].fromTime} - ${monday[i-1].toTime}\n ${monday[i-1].room}")
        }

        //tuesday
        val row4 = sheet.getRow(4)
        for(i in 1..tuesday.size){
            val cell = row4.createCell(i)
//            cell.setCellStyle(cellStyle)
            cell.setCellValue("${tuesday[i-1].subject}\n${tuesday[i-1].teacher}\n${tuesday[i-1].fromTime} - ${tuesday[i-1].toTime}\n ${tuesday[i-1].room}")
        }

        //wednesday
        val row5 = sheet.getRow(5)
        for(i in 1..wednesday.size){
            val cell = row5.createCell(i)
//            cell.setCellStyle(cellStyle)
            cell.setCellValue("${wednesday[i-1].subject}\n${wednesday[i-1].teacher}\n${wednesday[i-1].fromTime} - ${wednesday[i-1].toTime}\n ${wednesday[i-1].room}")
        }

        //thursday
        val row6 = sheet.getRow(6)
        for(i in 1..thursday.size){
            val cell = row6.createCell(i)
//            cell.setCellStyle(cellStyle)
            cell.setCellValue("${thursday[i-1].subject}\n${thursday[i-1].teacher}\n${thursday[i-1].fromTime} - ${thursday[i-1].toTime}\n ${thursday[i-1].room}")
        }

        //friday
        val row7 = sheet.getRow(7)
        for(i in 1..friday.size){
            val cell = row7.createCell(i)
//            cell.setCellStyle(cellStyle)
            cell.setCellValue("${friday[i-1].subject}\n${friday[i-1].teacher}\n${friday[i-1].fromTime} - ${friday[i-1].toTime}\n ${friday[i-1].room}")
        }










//        cellStyle.apply {
//            fillForegroundColor = HSSFColor.AQUA.index
//            fillPattern = HSSFCellStyle.SOLID_FOREGROUND
//            alignment = CellStyle.ALIGN_CENTER
//        }


        val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString(), "/TimeTable.xls")

        Log.e("asd", "Path - $filePath")

        try {
            if (!filePath.exists())
                filePath.createNewFile()

            val fileOutputStream = FileOutputStream(filePath)
            workbook.write(fileOutputStream)

            if (fileOutputStream != null) {
                fileOutputStream.flush()
                fileOutputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        workbook.close()

//        cell = null
//
//        //cell style for a cell
//        val cellStyle = workbook.createCellStyle()
//        cellStyle.apply {
//            fillForegroundColor = HSSFColor.AQUA.index
//            fillPattern = HSSFCellStyle.SOLID_FOREGROUND
//            alignment = CellStyle.ALIGN_CENTER
//        }
//
////        sheet = null
//        //create a new sheet in a workbook and assign a name to it
////        sheet = workbook.createSheet(EXCEL_SHEET_NAME)
//
//        // Generate column headings
//        val row: Row = sheet!!.createRow(0)
//
//        cell = row.createCell(0)
//        cell!!.setCellValue("First Name")
//        cell!!.cellStyle = cellStyle
//
//        cell = row.createCell(1)
//        cell!!.setCellValue("Last Name")
//        cell!!.cellStyle = cellStyle
//
//        cell = row.createCell(2)
//        cell!!.setCellValue("Phone Number")
//        cell!!.cellStyle = cellStyle
//
//        cell = row.createCell(3)
//        cell!!.setCellValue("Mail ID")
//        cell!!.cellStyle = cellStyle


    }

}
