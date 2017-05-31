package sokohuru.muchbeer.king.printertra;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import sokohuru.muchbeer.king.printertra.utils.PrinterCommands;
import sokohuru.muchbeer.king.printertra.utils.Utils;

public class MainActivity extends AppCompatActivity {

    EditText message;
    Button printbtn;

    byte FONT_TYPE;
    private static BluetoothSocket btsocket;
    private static OutputStream btoutputstream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = (EditText)findViewById(R.id.message);
        printbtn = (Button)findViewById(R.id.printButton);

        printbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                connect();
            }
        });

    }

    protected void connect() {
        if(btsocket == null){
            Intent BTIntent = new Intent(getApplicationContext(), DeviceList.class);
            this.startActivityForResult(BTIntent, DeviceList.REQUEST_CONNECT_BT);
        }
        else{
            OutputStream opstream = null;
            try {
                opstream = btsocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            btoutputstream = opstream;

            //print command
            try {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                btoutputstream = btsocket.getOutputStream();

                byte[] printformat = { 0x1B, 0*21, FONT_TYPE };
                btoutputstream.write(printformat);

                //print title
                printUnicode();
                //print normal text
                printTitle(message.getText().toString());
                printPhoto();
                printNewLine();
                printText("     >>>>   Thank you  <<<<     "); // total 32 char in a single line
                //resetPrint(); //reset printer
                printUnicode();
                printNewLine();
                printNewLine();

                btoutputstream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //print Title
    private void printTitle(String msg) {
        try {
            //Print config
            byte[] bb = new byte[]{0x1B,0x21,0x08};
            byte[] bb2 = new byte[]{0x1B,0x21,0x20};
            byte[] bb3 = new byte[]{0x1B,0x21,0x10};
            byte[] cc = new byte[]{0x1B,0x21,0x00};

            //btoutputstream.write(bb);
            //btoutputstream.write(bb2);
            btoutputstream.write(bb3);

            //set text into center
            btoutputstream.write(PrinterCommands.ESC_ALIGN_CENTER);
            btoutputstream.write(msg.getBytes());
            btoutputstream.write(PrinterCommands.LF);
            btoutputstream.write(PrinterCommands.LF);
            btoutputstream.write(cc);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print photo
    public void printPhoto() {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                    R.drawable.img);
            if(bmp!=null){
                byte[] command = Utils.decodeBitmap(bmp);
                printText(command);
            }else{
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    //print unicode
    public void printUnicode(){
        try {
            btoutputstream.write(PrinterCommands.ESC_ALIGN_CENTER);
            printText(Utils.UNICODE_TEXT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //print new line
    private void printNewLine() {
        try {
            btoutputstream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void resetPrint() {
        try{
            btoutputstream.write(PrinterCommands.ESC_FONT_COLOR_DEFAULT);
            btoutputstream.write(PrinterCommands.FS_FONT_ALIGN);
            btoutputstream.write(PrinterCommands.ESC_ALIGN_LEFT);
            btoutputstream.write(PrinterCommands.ESC_CANCEL_BOLD);
            btoutputstream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //print text
    private void printText(String msg) {
        try {
            // Print normal text
            btoutputstream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print byte[]
    private void printText(byte[] msg) {
        try {
            // Print normal text
            btoutputstream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(btsocket!= null){
                btoutputstream.close();
                btsocket.close();
                btsocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            btsocket = DeviceList.getSocket();
            if(btsocket != null){
                printText(message.getText().toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
