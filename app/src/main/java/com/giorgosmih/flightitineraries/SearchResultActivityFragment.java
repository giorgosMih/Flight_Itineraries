package com.giorgosmih.flightitineraries;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Arrays;

public class SearchResultActivityFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_search_result_activity, container, false);

        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            Flight[] result = (Flight[]) intent.getSerializableExtra(Intent.EXTRA_TEXT);
            ListView detailText = (ListView)rootView.findViewById(R.id.resultListView);

            detailText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CustomListViewAdapter adapter = (CustomListViewAdapter) parent.getAdapter();
                    Flight flightObj = adapter.getObjectAt(position);

                    Intent detailedIntent = new Intent(getContext(),DetailActivity.class);

                    detailedIntent.putExtra(Intent.EXTRA_TEXT,flightObj);
                    startActivity(detailedIntent);
                }
            });

            CustomListViewAdapter adapter = new CustomListViewAdapter(
                    getContext(),
                    R.layout.search_result_item,
                    Arrays.asList(result)
            );

            detailText.setAdapter(adapter);
        }

        return rootView;
    }
}
