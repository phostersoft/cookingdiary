package com.phostersoft.cookingdiary;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.phostersoft.cookingdiary.dataaccess.DBAdapter;
import com.phostersoft.cookingdiary.dataaccess.DBConstants;
import com.phostersoft.cookingdiary.utils.IndexedCursorAdaptor;

public class TagListView extends ListActivity {
	
	private DBAdapter dbAdapter;
	private ListView listView;
	private Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tag_list);
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		fillData();
		
		listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				viewRecipeWithTagList(view);
			}
		});
		
	}

	private void fillData() {
		cursor = dbAdapter.fetchTags();
		startManagingCursor(cursor);
		
		String[] from = { DBConstants.COLUMN_TAG_TAG };
		int[] to = {R.id.recipeText};
		IndexedCursorAdaptor cursorAdapter = new IndexedCursorAdaptor(
				this, R.layout.recipe_list_item, cursor, from, to);
		setListAdapter(cursorAdapter);
	}

	private void viewRecipeWithTagList(View view) {
		Intent intent = new Intent(this, RecipeListView.class);
		intent.putExtra("tagFilter", cursor.getInt(0));
		startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
		dbAdapter.close();
	    super.onDestroy();
	}
}
