package com.neurospeech.hypercube;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.neurospeech.hypercube.ui.HyperItemViewHolder;
import com.neurospeech.hypercube.ui.HyperViewHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;


/**
 * Created by akash.kava on 21-03-2016.
 */
public class HyperCubeApplication  {


    /**
     * We need to store layout from model and view holder from layout as we do not have this facility
     * directly available in android, hence the below maps are created
     *
     * modelLayouts and layoutViewHolders are declared as static to improve performance
     */
    /**
     * modelLayouts - stores the layout Id for given Model class
     */
    public static HashMap<Class,Integer> modelLayouts
            = new HashMap<>();


    /**
     * layoutViewHolders - stores the ViewHolder class for given layout Id (viewType)
     */
    public static HashMap<Integer,ViewHolderInfo> layoutViewHolders
            = new HashMap<>();


    public static void registerViewHolderType(Class<? extends HyperViewHolder<?>> viewHolderClass){
        HyperItemViewHolder ivh = (HyperItemViewHolder) viewHolderClass
                .getAnnotation(HyperItemViewHolder.class);
        if(ivh==null)
            throw new IllegalArgumentException("HyperItemViewHolder annotation is missing on ViewHolder class");

        // try to fetch modelType...

        Class modelType = null;

        Type t = viewHolderClass.getGenericSuperclass();

        ParameterizedType pt = (ParameterizedType)t;
        modelType = (Class)pt.getActualTypeArguments()[0];

        int id = modelLayouts.size() + 1;

        modelLayouts.put(modelType,id);

        ViewHolderInfo info = new ViewHolderInfo();
        info.layoutId = ivh.value();
        info.viewHolderClass = viewHolderClass;

        layoutViewHolders.put(id,info);

    }




    public void setLogger(AppLogger logger) {
        this.logger = logger;
    }

    private AppLogger logger;

    public void logError(String error){
        //Log.e("Error", error);
        if(logger!=null){
            logger.log("Error",error);
        }
    }

    public static HyperCubeApplication current;
    public static Application application;
    public static Activity activity;

    private Handler handler;


    public static HyperCubeApplication init(Application context){
        if(current==null) {
            application = context;
            current = new HyperCubeApplication();
            current.handler = new Handler();
            registerActivityLifecycle(application);
            current.logger = new AppLogger();
        }
        return current;
    }

    private static void registerActivityLifecycle(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                HyperCubeApplication.activity = activity;
            }

            @Override
            public void onActivityStarted(Activity activity) {
                HyperCubeApplication.activity = activity;
            }

            @Override
            public void onActivityResumed(Activity activity) {
                HyperCubeApplication.activity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if(activity== HyperCubeApplication.activity)
                    HyperCubeApplication.activity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                if(activity== HyperCubeApplication.activity)
                    HyperCubeApplication.activity = null;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if(activity== HyperCubeApplication.activity)
                    HyperCubeApplication.activity = null;
            }
        });
    }


    public void post(final Runnable action){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    action.run();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    public void post(final Runnable action, long milliSecondsDelay){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    action.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, milliSecondsDelay);
    }

    public static void confirm(
            Context context,
            String title,
            String message,
            final DialogInterface.OnClickListener clickListener){
        confirm(context,title,message,clickListener,null);
    }


    public static void confirm(
            Context context,
            String title,
            String message,
            final DialogInterface.OnClickListener clickListener,
            final DialogInterface.OnCancelListener cancelListener){
        AlertDialog.Builder builder
                = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickListener.onClick(dialog,which);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(cancelListener!=null){
                            cancelListener.onCancel(dialog);
                        }
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }

    public static String toString(Throwable ex){
        String msg = ex.toString();
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);

            ex.printStackTrace(pw);

            pw.flush();

            msg = sw.toString();

        }catch (Exception e){

        }finally{
            try {
                if (pw != null)
                    pw.close();
                if (sw != null)
                    sw.close();
            }catch (Exception doNothing){

            }
        }
        return msg;
    }

    public void logError(Exception ex) {
        String error = toString(ex);
        logError(error);
    }


    public static class ViewHolderInfo{

        public Class viewHolderClass;

        public int layoutId;

    }
}
