package com.example.slam;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;

public class PollReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS", Locale.US);
		//String timestamp = dateFormat.format(new Date());
		
		long timestamp = System.currentTimeMillis();
		JSONObject record = new JSONObject();
		try{
			record.put("timestamp", timestamp);
			record.put("mark", MainActivity.markupNum);
			
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			List<ScanResult> networks;
			int ncount = 0;
		
			wifi.startScan();
			networks = wifi.getScanResults();
			if(networks != null)
				ncount = networks.size();
			record.put("scan", ncount);
			
			JSONArray results = new JSONArray();
			JSONObject wifi_info = new JSONObject();
			ScanResult access_point;
			
			Log.i("PollReciever","Available networks: " + ncount);
			
			String netinfo = "";
			String[] uniqueMAC = new String[ncount + 1];
			boolean notunique = false;
			
			for(int ap = 0; ap < ncount; ap++){
				access_point = networks.get(ap);
				
				for(int prev = 0; prev < ap; prev++){
					if(access_point.BSSID.equals(uniqueMAC[prev])){
						notunique = true;
						break;
					}
				}
				if(notunique){
					notunique = false;
					continue;
				}
				wifi_info.put("ssid", access_point.SSID);
				wifi_info.put("bssid", access_point.BSSID);
				wifi_info.put("level", access_point.level);
				results.put(wifi_info);
				
				netinfo += ("\t[" + access_point.level + " dBm | " + access_point.BSSID + "]\t" + access_point.SSID + "\n");
				Log.i("PollReciever","Network " + ap + ": " + access_point.SSID);
			}
			
			String info = "";
			if(wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
				info += ("Connected: \tYES\n");
			else
				info += ("Connected: \tNO\n");
			info += ("Available: \t" + ncount + "\n");
			info += ("Mark: \t" + MainActivity.markupNum + "\n");
			info += ("Elapsed: \t" + (timestamp - MainActivity.startTime)/1000.0 + " seconds\n");
			info += ("Networks:\n" + netinfo);
			
			if(MainActivity.mThis != null){
				((TextView)MainActivity.mThis.findViewById(R.id.wfinfo)).setText(info);
			}
			
			record.put("networks",results);
			writeRecord(record);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void writeRecord(JSONObject record){
		try{
			FileOutputStream fout = new FileOutputStream(MainActivity.recordfile, true);
			OutputStreamWriter out = new OutputStreamWriter(fout);
			
			out.append(record.toString() + "\n");
			out.close();
			fout.close();
			
			Log.i("PollReceiver","Wrote to: " + MainActivity.recordfile.getAbsolutePath());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
