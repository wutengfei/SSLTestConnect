package cn.org.cctc.ssltestconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import cn.com.syan.libcurl.CurlHttpClient;
import cn.com.syan.libcurl.SyanCurlContext;
import cn.com.syan.libcurl.model.CertType;
import cn.com.syan.libcurl.request.Request;
import cn.com.syan.libcurl.response.Response;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private EditText editText2;
    private EditText editText3;
    private EditText editText4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText2);
        editText3 = findViewById(R.id.editText3);
        editText4 = findViewById(R.id.editText4);

    }

    public void connectServer(View view) {
        String IPServer = editText.getText().toString().trim();
        String porterServer = editText2.getText().toString().trim();
        final String urlString = "http://" + IPServer + ":" + porterServer + "/v1/version";

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    //设置请求方法
                    connection.setRequestMethod("GET");
                    //设置连接超时时间（毫秒）
                    connection.setConnectTimeout(5000);
                    //设置读取超时时间（毫秒）
                    connection.setReadTimeout(5000);

                    //返回输入流
                    InputStream in = connection.getInputStream();

                    //读取输入流
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    showMessageDialog("服务端返回数据:", result.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {//关闭连接
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public void showMessageDialog(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(title);
                builder.setMessage(message);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        });


    }


    public void connectGateway(View view) {
        String IPGateway = editText3.getText().toString().trim();
        String porterGateway = editText4.getText().toString().trim();
        String interfaceName = "/v1/version";

        String path1 = "crt.CN.ABC+ltd..0912test.cer";
        String path2 = "asign.pem";
        String path3 = "asignpvk.pem";
        String path4 = "bsign.pem";
        String path5 = "bsignpvk.pem";

        String abcaPath = this.getCacheDir().getAbsolutePath() + "/" + path1;
        String asignPath = this.getCacheDir().getAbsolutePath() + "/" + path2;
        String asignpvkPath = this.getCacheDir().getAbsolutePath() + "/" + path3;
        String bsignPath = this.getCacheDir().getAbsolutePath() + "/" + path4;
        String bsignpvkPath = this.getCacheDir().getAbsolutePath() + "/" + path5;

        AssetToSD(this, path1, abcaPath);
        AssetToSD(this, path2, asignPath);
        AssetToSD(this, path3, asignpvkPath);
        AssetToSD(this, path4, bsignPath);
        AssetToSD(this, path5, bsignpvkPath);

        CurlHttpClient client = new CurlHttpClient.Builder().
                setAuthType(2).
                setCaInfo(abcaPath).
                setCertPath(asignPath).
                setKeypath(asignpvkPath).
                setEncCertType(CertType.PEM).
                setEncKeyType(CertType.PEM).
                setEncCertPath(bsignPath).
                setEncKeypath(bsignpvkPath).
                setCertType(CertType.PEM).
                setKeyType(CertType.PEM).
                setSSLVersion(SyanCurlContext.CURL_SSLVERSION_CNCAv1_1).
                setConnectTimeout(8).
                setTimeout(8).
                setHost(IPGateway).
                setPort(Integer.parseInt(porterGateway)).
                build();


        Request request = new Request.Builder()
                .get()
                .params("get()Param", "test")
                .url(interfaceName)
                .build();
        try {
            Response response = client.excute(request);
            showMessageDialog("SM2 POST(双向认证)请求成功",
                    "请求头 " + request.getHttpHeaders() + "\n" +
                            "请求体" + request.getParams() + "\n" +
                            "响应头" + response.getHeader() + "\n" +
                            "响应体" + response.getBodyAsString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void AssetToSD(Context context, String assetpath, String SDpath) {

        AssetManager asset = context.getAssets();
        //循环的读取asset下的文件，并且写入到SD卡
        String[] filenames = null;
        FileOutputStream out = null;
        InputStream in = null;
        try {
            filenames = asset.list(assetpath);
            if (filenames.length > 0) {//说明是目录
                //创建目录
                for (String fileName : filenames) {
                    AssetToSD(context, assetpath + "/" + fileName, SDpath + "/" + fileName);
                }
            } else {//说明是文件，直接复制到SD卡
                File SDFlie = new File(SDpath);
                if (!SDFlie.exists()) {
                    SDFlie.createNewFile();
                }
                //将内容写入到文件中
                in = asset.open(assetpath);
                out = new FileOutputStream(SDFlie);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = in.read(buffer)) != -1) {
                    out.write(buffer, 0, byteCount);
                }
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
//                asset.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
