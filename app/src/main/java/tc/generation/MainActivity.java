package tc.generation;
 
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.Manifest;
import java.security.Permission;
import android.content.Intent;

public class MainActivity extends Activity {
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
    }
	public void requestPermission(View v){
        String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE};
        this.requestPermissions(permissions,1503);
    }
    public void openSource(View v){
        Intent i=new Intent(this,WebviewActivity.class);
        i.putExtra("url","https://github.com/1503Dev/generation-for-android");
        startActivity(i);
    }
}
