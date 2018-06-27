package inshow.carl.com.ota_tool.csd;

import java.util.HashMap;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * Created by chendong on 2018/6/26.
 */

public class PressedAdapter {

    private List<ScanResult> results;
    private HashMap<ScanResult,Boolean> map = new HashMap<>();

    public PressedAdapter(List<ScanResult> results) {
        this.results = results;
    }

    public void notifyDataChanged(List<ScanResult> results){

    }


}
