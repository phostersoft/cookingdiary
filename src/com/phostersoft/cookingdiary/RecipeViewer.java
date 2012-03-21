package com.phostersoft.cookingdiary;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phostersoft.cookingdiary.dataaccess.DBAdapter;
import com.phostersoft.cookingdiary.utils.ImageUtils;

public class RecipeViewer extends Activity {

	private DBAdapter dbAdapter;
	private TextView titleText;
	private TextView materialText;
	private TextView stepsText;
	private TextView sourceText;
	private TextView noteText;
	private TextView tagsText;
	private ImageView photoView;
	private int recipeId;
	
	Cursor c;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_viewer);
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		
		titleText = (TextView) findViewById(R.id.titleTextView);
		materialText = (TextView) findViewById(R.id.materialTextView);
		stepsText = (TextView) findViewById(R.id.stepsTextView);
		sourceText = (TextView) findViewById(R.id.sourceTextView);
		noteText = (TextView) findViewById(R.id.noteTextView);
		tagsText = (TextView) findViewById(R.id.tagsTextView);
		photoView = (ImageView) findViewById(R.id.photoView);
		
		Intent intent = getIntent();
		
		if (savedInstanceState == null) {
			recipeId = intent.getIntExtra("recipeId", -1);
		} else {
			recipeId = (Integer) savedInstanceState.getSerializable("id");
		}
		initView();		
	}
	
	private void initView() {
		c = dbAdapter.fetchRecipe(recipeId);
		startManagingCursor(c);
		
		titleText.setText(c.getString(c.getColumnIndex("title")));
		displaySection(c.getString(c.getColumnIndex("material")), materialText);
		displaySection(c.getString(c.getColumnIndex("steps")), stepsText);
		displaySection(c.getString(c.getColumnIndex("source")), sourceText);
		displaySection(c.getString(c.getColumnIndex("note")), noteText);
		
		String tags = "";
		Cursor c2 = dbAdapter.fetchRecipeTags(recipeId);
		startManagingCursor(c2);
		if (c2.getCount() > 0) {
			do {
				tags += c2.getString(1) + ",";
			} while (c2.moveToNext());
			tags = tags.substring(0, tags.length() - 1);
		}
		displaySection(tags, tagsText);
		
		c2 = dbAdapter.fetchImagesForRecipe(recipeId);
		startManagingCursor(c2);
		if (c2 != null && !c2.isAfterLast()) {
			Uri uri = Uri.parse(c2.getString(1));
			photoView.setImageBitmap(ImageUtils.decodeImage(
					getContentResolver(), uri));
		} else {
			// clear photoView if there's no image
			photoView.setImageBitmap(null);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_recipe_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit_recipe:
			editRecipe();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putSerializable("id", new Integer(recipeId));
	}
	
	private void editRecipe() {
    	Intent intent = new Intent(this, RecipeEditorView.class);
    	intent.putExtra("recipeId", recipeId);
    	startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		initView();
	}
	
	@Override
	protected void onDestroy() {
		dbAdapter.close();
	    super.onDestroy();
	}
	
	private void displaySection(String content, TextView view) {
		LinearLayout parent = (LinearLayout) view.getParent();
        boolean visibility = content != null && content.length() > 0;
        view.setText(content);
        parent.setVisibility(visibility ? View.VISIBLE : View.GONE);
	}
}
