package com.phostersoft.cookingdiary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.phostersoft.cookingdiary.dataaccess.DBAdapter;
import com.phostersoft.cookingdiary.utils.ImageUtils;

public class RecipeEditorView extends Activity{
	
	private DBAdapter dbAdapter;
	
	private int recipeId;
	private Uri photoUri;
	private EditText titleText;
	private EditText materialText;
	private EditText stepsText;
	private EditText sourceText;
	private EditText noteText;
	private EditText tagsText;
	private ImageView photoView;
	private Button addPhotoButton;
	private Uri tempUri;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_editor);
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		
		titleText = (EditText) findViewById(R.id.recipeTitle);
		materialText = (EditText) findViewById(R.id.recipeMaterial);
		stepsText = (EditText) findViewById(R.id.recipeSteps);
		sourceText = (EditText) findViewById(R.id.recipeSource);
		noteText = (EditText) findViewById(R.id.recipeNote);
		tagsText = (EditText) findViewById(R.id.recipeTags);
		photoView = (ImageView) findViewById(R.id.photoView);
		addPhotoButton = (Button) findViewById(R.id.addPhoto);
		
		registerForContextMenu(photoView);
		
		if (savedInstanceState != null) {
			restoreRecipeFromSavedState(savedInstanceState);
		} else {
			populateRecipeFromDB();
		}
		
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		String title = titleText.getText().toString();
		String steps = stepsText.getText().toString();
		String material = materialText.getText().toString();
		String source = sourceText.getText().toString();
		String note = noteText.getText().toString();
		String tags = tagsText.getText().toString();
		
		outState.putSerializable("id", new Integer(recipeId));
		outState.putSerializable("title", title);
		outState.putSerializable("steps", steps);
		outState.putSerializable("material", material);
		outState.putSerializable("source", source);
		outState.putSerializable("note", note);
		outState.putSerializable("tags", tags);
	}
	
	private boolean validateRecipe() {
		if (titleText.getText().toString().trim().length() == 0) {
			Toast toast = Toast.makeText(this, R.string.recipe_error_notitle, 5);
			toast.show();
			return false;
		}
		Scanner scanner = new Scanner(tagsText.getText().toString().trim());
		scanner.useDelimiter(",");
		while (scanner.hasNext()) {
			String tag = scanner.next();
			if(tag.trim().isEmpty()) {
				Toast toast = Toast.makeText(this, R.string.recipe_error_tags, 5);
				toast.show();
				return false;
			}
		}
		return true;
	}
	
	public void saveRecipe(View view) {
		if (!validateRecipe()) {
			return;
		}
		
		long id = -1;
		if (recipeId > -1) {
			id = dbAdapter.updateRecipe(recipeId, 
					titleText.getText().toString(), 
					stepsText.getText().toString(),
					materialText.getText().toString(), 
					sourceText.getText().toString(),
					noteText.getText().toString());
		} else {
			id = dbAdapter.createRecipe(titleText.getText().toString(), 
					stepsText.getText().toString(),
					materialText.getText().toString(), 
					sourceText.getText().toString(),
					noteText.getText().toString());
		}
		Cursor tagCursor = dbAdapter.fetchRecipeTags((int) id);
		startManagingCursor(tagCursor);
		if (tagCursor.getCount() > 0) {
			do {
				dbAdapter.removeRecipeTag((int)id, tagCursor.getInt(0)); 
				dbAdapter.deleteTagIfNoRecipe(tagCursor.getInt(0));
			} while (tagCursor.moveToNext());
		}
		String tags = tagsText.getText().toString().trim();
		if (tags.length() > 0) {
			String[] tagsArray = tags.split(",");
			for (String tag : tagsArray) {
				tag = tag.trim();
				long tagId = dbAdapter.createTagIfNotExists(tag);
				dbAdapter.tagRecipe((int)id, (int)tagId);
			}
		}
		Cursor c = dbAdapter.fetchImagesForRecipe((int)id);
		startManagingCursor(c);
		if (c.getCount() > 0) {
			do {
				int photoId = c.getInt(0);
				dbAdapter.removePhoto(photoId);
			} while (c.moveToNext());
		}
		if (photoUri != null) {
			dbAdapter.addPhotoForRecipe((int)id, photoUri.toString());
		}
		setResult(0);
		finish();
	}
	
	public void discardRecipe(View view) {
		setResult(0);
		finish();
	}
	
	private File getTempFile(Context context) {
		final File path = new File(Environment.getExternalStorageDirectory(),
				"CookingDiary");
		if (!path.exists()) {
			path.mkdirs();
		}
		File photofile = new File(path, "image" + System.currentTimeMillis()
				+ ".jpg");
		if (!photofile.exists()) {
			try {
				photofile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return photofile;
	}
	
	public void addPhoto(View view) {
		Intent gallIntent = new Intent(Intent.ACTION_PICK);
		gallIntent.setType("image/*"); 
		Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		tempUri = Uri.fromFile(getTempFile(this));
		camIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);   
		
		PackageManager packageManager = getPackageManager();
		final List<Intent> yourIntentsList = new ArrayList<Intent>();
		List<String> itemList = new ArrayList<String>();

		List<ResolveInfo> listCam = packageManager.queryIntentActivities(camIntent, 0);
		for (ResolveInfo res : listCam) {
		    final Intent finalIntent = new Intent(camIntent);
		    finalIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
		    yourIntentsList.add(finalIntent);
		    itemList.add(res.activityInfo.loadLabel(packageManager).toString());
		}

		List<ResolveInfo> listGall = packageManager.queryIntentActivities(gallIntent, 0);
		for (ResolveInfo res : listGall) {
		    final Intent finalIntent = new Intent(gallIntent);
		    finalIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
		    yourIntentsList.add(finalIntent);
		    itemList.add(res.activityInfo.loadLabel(packageManager).toString());
		}
		
		final CharSequence[] items = itemList.toArray(new CharSequence[1]);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick an action");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	int action = 1;
		    	if (yourIntentsList.get(item).hasExtra(MediaStore.EXTRA_OUTPUT)) {
		    		action = 2;
		    	}
		        startActivityForResult(yourIntentsList.get(item), action);
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void populateRecipeFromDB() {
		recipeId = getIntent().getIntExtra("recipeId", -1);
		if (recipeId > -1) {
			Cursor c = dbAdapter.fetchRecipe(recipeId);
			startManagingCursor(c);
			titleText.setText(c.getString(c.getColumnIndex("title")));
			materialText.setText(c.getString(c.getColumnIndex("material")));
			stepsText.setText(c.getString(c.getColumnIndex("steps")));
			sourceText.setText(c.getString(c.getColumnIndex("source")));
			noteText.setText(c.getString(c.getColumnIndex("note")));
			String tags = "";
			Cursor c2 = dbAdapter.fetchRecipeTags(recipeId);
			startManagingCursor(c2);
			if (c2.getCount() > 0) {
				do {
					tags += c2.getString(1) + ",";
				} while (c2.moveToNext());
				tags = tags.substring(0, tags.length() - 1);
			}
			tagsText.setText(tags);
			c = dbAdapter.fetchImagesForRecipe(recipeId);
			if (c != null && !c.isAfterLast()) {
				startManagingCursor(c);
				photoUri = Uri.parse(c.getString(1));
				photoView.setImageBitmap(ImageUtils.decodeImage(
						getContentResolver(), photoUri));
				addPhotoButton.setEnabled(false);
			}
		}
	}
	
	private void restoreRecipeFromSavedState(Bundle savedState) {
		recipeId = (Integer) savedState.getSerializable("id");
		titleText.setText((String) savedState.getSerializable("title"));
		materialText.setText((String) savedState.getSerializable("material"));
		stepsText.setText((String) savedState.getSerializable("steps"));
		sourceText.setText((String) savedState.getSerializable("source"));
		tagsText.setText((String) savedState.getSerializable("tags"));
		noteText.setText((String) savedState.getSerializable("note"));
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
        	photoUri = data.getData();
        	photoView.setImageBitmap(ImageUtils.decodeImage(
        			getContentResolver(), photoUri));
        	addPhotoButton.setEnabled(false);
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
        	photoUri = tempUri;
        	photoView.setImageBitmap(ImageUtils.decodeImage(
        			getContentResolver(), photoUri));
        	addPhotoButton.setEnabled(false);
        }
    }
    
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.delete_photo_context_menu, menu);
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete:
			photoUri = null;
			photoView.setImageBitmap(null);
			addPhotoButton.setEnabled(true);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
	    super.onDestroy();
	}
}
