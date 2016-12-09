package com.sunrc.clientchat;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ChatRoom extends AppCompatActivity implements ListViewAdapter.ListBtnClickListener{

    private Socket s;
    private Handler networkdHandler;
    private EditText chatMsg;
    private Button chatBtn;
    private PrintWriter pw;
    private BufferedReader in;
    static String namev = "";
    @SuppressLint("NewApi")
    private ArrayList<String> msgPool;
    private ArrayList<String> userListPool;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Log.i("알림","ChatRoom onCreate");

        if(Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        list = (ListView)findViewById(R.id.chat_list);
        chatMsg = (EditText)findViewById(R.id.chat_msg);
        chatBtn = (Button)findViewById(R.id.chat_msgBtn);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String hostv = bundle.getString("host");
        final String namev = bundle.getString("name");

        Log.i("알림", namev);

        int portv = bundle.getInt("port");
        msgPool = new ArrayList<>();
        userListPool = new ArrayList<>();

        networkdHandler = new Handler();
        testServer(hostv, namev, portv);
        chatResponse(namev);

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = chatMsg.getText().toString().trim();
                pw.println("talk/chat/all/["+namev+"]"+msg);
                chatMsg.setText("");
                chatMsg.requestFocus();
            }
        });
    }

    private boolean testServer(String hostv, String namev, int portv) {
        try {
            s = new Socket(hostv,portv);
            pw = new PrintWriter(new BufferedOutputStream(s.getOutputStream()),true);
            pw.println("room/chat/all/"+namev+"/");

            Log.i("알림","testServer");

            return true;
        }catch (IOException e) {
            Log.i("알림",e.getMessage().toString());
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();

            e.printStackTrace();

            return false;
        }
    }

    private void chatResponse(String namev) {

        final String nickName = namev;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    in = new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
                    while(true) {
                        final String protocol = in.readLine();
                        String msgv = "";

                        // UserList 일 경우 분기 처리 필요
                        StringTokenizer stz = new StringTokenizer(protocol,"/");
                        final String protocol1 = stz.nextToken();

                        if (protocol1.equals("UserList"))
                        {
                            Log.i("nickName", nickName);

                            //msgPool.clear();

                            String tmpValue;
                            for(int i=0; i<=stz.countTokens(); i++) {
                                tmpValue = stz.nextToken();
                                if (!tmpValue.equals(nickName))
                                {
                                    userListPool.add(tmpValue);
                                }
                            }
                        }
                        else if (protocol1.equals("talk"))
                        {
                            String protocol2 = stz.nextToken();
                            String protocol3 = stz.nextToken();
                            String protocol4 = stz.nextToken();
                            //String msgv = protocol2 + "]" + protocol3;
                            msgv = protocol4;

                            Log.i("protocol1",protocol1);
                            Log.i("protocol2",protocol2);
                            Log.i("protocol3",protocol3);
                            Log.i("protocol4",protocol4);
                            msgPool.add(msgv);
                        }

                        networkdHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(protocol1.equals("UserList")) {

                                    ListViewAdapter adapter;
                                    ArrayList<ListViewItem> items = new ArrayList<ListViewItem>() ;

                                    // items 로드.
                                    loadItemsFromDB(items, userListPool);

                                    // Adapter 생성
                                    adapter = new ListViewAdapter(ChatRoom.this, R.layout.activity_chat_userlist, items, ChatRoom.this);

                                    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChatRoom.this, android.R.layout.simple_list_item_1, userListPool);
                                    list.setAdapter(adapter);
                                }

                                if(protocol1.equals("talk")) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChatRoom.this, android.R.layout.simple_list_item_1, msgPool);
                                    list.setAdapter(adapter);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean loadItemsFromDB(ArrayList<ListViewItem> list, ArrayList<String> userListPool) {
        ListViewItem item ;
        int i;

        if (list == null) {
            list = new ArrayList<ListViewItem>() ;
        }

        // 순서를 위한 i 값을 1로 초기화.
        i = 1 ;
        for (String userItem : userListPool) {
            item = new ListViewItem();
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_launcher)) ;
            item.setText(userItem.toString());
            list.add(item);
            i++;
        }

        return true ;
    }

    @Override
    public void onListBtnClick(int position) {
        Toast.makeText(this, Integer.toString(position+1) + "번 아이템이 선택되었습니다.", Toast.LENGTH_SHORT).show() ;
    }
}
