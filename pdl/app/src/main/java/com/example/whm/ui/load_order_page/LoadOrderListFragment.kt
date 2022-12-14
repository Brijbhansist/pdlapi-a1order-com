package com.example.whm.ui.load_order_page

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.R
import com.example.myapplication.com.example.whm.AppPreferences
import com.example.myapplication.com.example.whm.ui.load_order_page.LoadOrderListAdapter
import com.example.myapplication.com.example.whm.ui.load_order_page.LoadOrderModel
import org.json.JSONObject
import java.io.IOException

class LoadOrderListFragment : Fragment() {
    private val LoadorderList = ArrayList<LoadOrderModel>()
    private lateinit var LoadorderAdapter: LoadOrderListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loadorderlists, container, false)
        view.requestFocus()
        view.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                }
            }
            true
        })
        if(AppPreferences.internetConnectionCheck(this.context)){

            val recyclerView: RecyclerView = view.findViewById(R.id.load_order)
            
            LoadorderAdapter = LoadOrderListAdapter(LoadorderList, this.context)

            val layoutManager = LinearLayoutManager(this.context)

            recyclerView.layoutManager = layoutManager
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = LoadorderAdapter

            val pDialog = SweetAlertDialog(this.context, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = "Fetching ..."
            pDialog.setCancelable(false)
            pDialog.show()

            val Jsonarra = JSONObject()
            val JSONObj = JSONObject()
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            var empautoid = preferences.getString("EmpAutoId", "")
            var accessToken = preferences.getString("accessToken", "")
            JSONObj.put("requestContainer",Jsonarra.put("deviceID",
                Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)))
            val queues = Volley.newRequestQueue(this.context)
            JSONObj.put("requestContainer", Jsonarra.put("appVersion", AppPreferences.AppVersion))
            JSONObj.put("requestContainer", Jsonarra.put("userAutoId", empautoid))
            JSONObj.put("requestContainer", Jsonarra.put("accessToken", accessToken))
            JSONObj.put("requestContainer", Jsonarra.put("accessToken", accessToken))
            val resorderno = JsonObjectRequest(
                Request.Method.POST,
                AppPreferences.BASEURL + AppPreferences.GET_ASSIGN_ORDER_LIST,
                JSONObj,
                { response ->
                    val resobj = (response.toString())
                    val responsemsg = JSONObject(resobj.toString())
                    val resultobj = JSONObject(responsemsg.getString("d"))
                    val resmsg = resultobj.getString("responseMessage")
                    val rescode = resultobj.getString("responseCode")
                    if (rescode == "201") {
                        LoadorderList.clear()
                        LoadorderAdapter.notifyDataSetChanged()
                        val jsondata = resultobj.getJSONArray("responseData")
                        for (i in 0 until jsondata.length()) {
                            val OrderNo = jsondata.getJSONObject(i).getString("ONo")
                            val PB = jsondata.getJSONObject(i).getInt("PB")
                            val Stoppage = jsondata.getJSONObject(i).getString("ST")
                            DataBindLoadorder(
                                OrderNo,
                                PB,
                                Stoppage
                            )
                        }
                        if(pDialog!=null){
                            if(pDialog.isShowing){
                                pDialog.dismiss()
                            }
                        }
                    } else {
//                        SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE).setContentText(
//                            resmsg.toString()
//                        ).show()
                        val alerts = AlertDialog.Builder(this.context)
                        alerts.setMessage(resmsg.toString())
                        alerts.setCancelable(false)
                        alerts.setPositiveButton("ok", null)
                        val dialog: AlertDialog = alerts.create()
                        dialog.show()
                        if(pDialog!=null){
                            if(pDialog.isShowing){
                                pDialog.dismiss()
                            }
                        }
                    }
                },
                { response ->
                    Log.e("onError", error(response.toString()))
                    if(pDialog!=null){
                        if(pDialog.isShowing){
                            pDialog.dismiss()
                        }
                    }
                })
            resorderno.retryPolicy = DefaultRetryPolicy(
                10000000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            try {
                queues.add(resorderno)
            } catch (e: IOException) {
                Toast.makeText(this.context, "Server Error", Toast.LENGTH_LONG).show()
            }
        }
        else{
            val dialog = context?.let { Dialog(it) }
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog?.setContentView(com.example.myapplication.R.layout.dailog_log)
            val btDismiss = dialog?.findViewById<Button>(com.example.myapplication.R.id.btDismissCustomDialog)
            btDismiss?.setOnClickListener {
                dialog.dismiss()
                this.findNavController().navigate(com.example.myapplication.R.id.nav_home)
            }
            dialog?.show()
        }
        return view
    }
    private fun DataBindLoadorder(Ono: String, PackedBoxes: Int, Stoppage: String) {
        var Loadorder = LoadOrderModel(Ono, PackedBoxes, Stoppage)
        LoadorderList.add(Loadorder)
        LoadorderAdapter.notifyDataSetChanged()
    }

}