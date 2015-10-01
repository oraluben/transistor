/**
 * InfosheetActivityFragment.java
 * Implements the infosheet fragment of the infosheet activity
 * This fragment shows a webview
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package org.y20k.transistor;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * InfosheetActivityFragment class
 */
public class InfosheetActivityFragment extends Fragment {

    /* Keys */
    public static final String CONTENT = "content";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        String content = intent.getExtras().getString(CONTENT);

        // inflate rootview from xml
        View rootView = inflater.inflate(R.layout.fragment_infosheet, container, false);

        // find web view
        TextView textView = (TextView) rootView.findViewById(R.id.infosheet_textview);

        // display html in webview
        textView.setText(Html.fromHtml(content));

        // return view
        return rootView;
    }

}