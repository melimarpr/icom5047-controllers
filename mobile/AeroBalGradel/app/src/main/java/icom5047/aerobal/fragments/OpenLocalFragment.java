package icom5047.aerobal.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aerobal.data.objects.Experiment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.Arrays;

import icom5047.aerobal.activities.OpenActivity;
import icom5047.aerobal.activities.R;
import icom5047.aerobal.resources.Keys;

/**
 * Created by enrique on 4/25/14.
 */
public class OpenLocalFragment extends Fragment implements AdapterView.OnItemClickListener{

    private ProgressBar progressBar;
    private ListView listView;
    private File baseRoot;
    private OpenActivity openActivity;
    private TextView emptyFolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_local, container, false);
        listView = (ListView) view.findViewById(R.id.fragOpenLocalListView);
        progressBar = (ProgressBar) view.findViewById(R.id.fragOpenLocalProgress);
        emptyFolder = (TextView) view.findViewById(R.id.fragOpenLocalEmptyFolder);
        Typeface face = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        emptyFolder.setTypeface(face);
        baseRoot = (File) getArguments().getSerializable(Keys.BundleKeys.FileRoot);
        openActivity = (OpenActivity) this.getActivity();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        FileFilter aeroDirectoryFilter = new FileFilter() {
            public boolean accept(File file) {
                if(file.isDirectory())
                    return true;
                else if(file.isFile()){
                    String ext = getFileExtension(file);
                    if(ext != null)
                        return ext.equals("aero");
                }
                return false;
            }
        };
        File[] listFile = baseRoot.listFiles(aeroDirectoryFilter);
        Arrays.sort(listFile);
        if(listFile.length != 0){
            //Filter .aero and folders
            listView.setAdapter(new ArrayAdapter<File>(this.getActivity(), android.R.layout.simple_list_item_1, listFile){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    File file = this.getItem(position);
                    LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
                    View rowLayout = inflater.inflate(R.layout.row_file_dir, parent, false);

                    ImageView imageView = (ImageView) rowLayout.findViewById(R.id.rowFileDirImg);
                    TextView textView = (TextView) rowLayout.findViewById(R.id.rowFileDirText);
                    Typeface face = Typeface.createFromAsset(openActivity.getAssets(), "fonts/Roboto-Thin.ttf");
                    textView.setTypeface(face);
                    textView.setText(file.getName());
                    if(file.isDirectory()){
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_folder));
                    }
                    else{
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_file));
                    }


                    return rowLayout;
                }
            });
            listView.setOnItemClickListener(this);
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        } else{
            progressBar.setVisibility(View.GONE);
            emptyFolder.setVisibility(View.VISIBLE);
        }


    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File currFile = (File) parent.getItemAtPosition(position);
        if(currFile.isDirectory()){
            OpenLocalFragment openLocalFragment = new OpenLocalFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Keys.BundleKeys.FileRoot, currFile);
            openLocalFragment.setArguments(bundle);
            openActivity.loadFragmentIntoActivity(openLocalFragment);
        }
        else{
            try{
                InputStream file = new FileInputStream(currFile);
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer);
                try{
                    Experiment experiment = (Experiment) input.readObject();

                    openActivity.setActivityResult(experiment);
                }finally {
                    file.close();
                    buffer.close();
                    input.close();
                }
            } catch (FileNotFoundException e) {
                Log.e("File:", "File not Fount");
            } catch (StreamCorruptedException e) {
                Log.e("File:", "Stream Corrupted");
            } catch (IOException e) {
                Log.e("File:", "IO Error");
            } catch (ClassNotFoundException e) {
                Log.e("File:", "Incorrect file (Class Cast)");
            }

        }
    }

    public static String getFileExtension(File f){
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }


}
