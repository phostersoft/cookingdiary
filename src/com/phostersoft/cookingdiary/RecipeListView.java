package com.phostersoft.cookingdiary;

import com.phostersoft.cookingdiary.R;
import com.phostersoft.cookingdiary.dataaccess.DBAdapter;
import com.phostersoft.cookingdiary.utils.IndexedCursorAdaptor;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

public class RecipeListView extends ListActivity {
	public static final int INSERT_ID = Menu.FIRST;
	
	private DBAdapter dbAdapter;
	private Cursor c;
	private TextWatcher textWatcher;
	private ListView listView;
	private IndexedCursorAdaptor recipes;
	private EditText filterText;
	private int tagFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			tagFilter = getIntent().getIntExtra("tagFilter", -1);
		} else {
			restoreState(savedInstanceState);
		}
		
		setContentView(R.layout.recipe_list);
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		fillData();
		
		listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				viewRecipe(view);
			}
		});
		
		textWatcher = new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			public void afterTextChanged(Editable s) {
				filterText(s.toString());
			}
		};
		
		filterText = (EditText) findViewById(R.id.filter);
		filterText.addTextChangedListener(textWatcher);
		
		registerForContextMenu(listView);
		
	}
	
	private void restoreState(Bundle savedInstanceState) {
		filterText.setText(
				(String) savedInstanceState.getSerializable("filterText"));
		tagFilter = (Integer) 
				savedInstanceState.getSerializable("tagFilter");
		filterText(filterText.getText().toString());
	}

	private void filterText(String string) {
		recipes.getFilter().filter(string);
		recipes.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, "New recipe");
		return result;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.recipe_list_context_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case INSERT_ID:
			createNewRecipe();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.edit:
			editRecipe(menuInfo.id);
			return true;
		case R.id.delete:
			deleteRecipe(menuInfo.id);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void createNewRecipe() {
    	Intent intent = new Intent(this, RecipeEditorView.class);
    	startActivity(intent);
	}

	private void fillData() {
		c = dbAdapter.fetchRecipeWithTagByTitle(null, tagFilter);
		startManagingCursor(c);
		
		FilterQueryProvider fqp = new FilterQueryProvider() {
			
			public Cursor runQuery(CharSequence constraint) {
				c = dbAdapter.fetchRecipeWithTagByTitle(constraint == null ? null : 
					constraint.toString(), tagFilter);
				startManagingCursor(c);
				return c;
			}
		};
		
		String[] from = new String[] {"title"};
		int[] to = new int[] {R.id.recipeText};
		
		recipes = 
				new IndexedCursorAdaptor(this, 
						R.layout.recipe_list_item, c, from, to);
		recipes.setFilterQueryProvider(fqp);
		
		setListAdapter(recipes);
		if (tagFilter == -1 && c.getCount() == 0) {
			TextView empty = (TextView) getListView().getEmptyView();
			empty.setText(R.string.recipelist_norecipe);
		}
	}
	
	public void viewRecipe(View view) {
    	Intent intent = new Intent(this, RecipeViewer.class);
    	intent.putExtra("recipeId", c.getInt(0));
    	startActivity(intent);
	}
	
	public void editRecipe(long id) {
    	Intent intent = new Intent(this, RecipeEditorView.class);
    	intent.putExtra("recipeId", c.getInt(0));
    	startActivity(intent);
	}
	
	public void deleteRecipe(long id) {
		Cursor c2 = dbAdapter.fetchRecipeTags(c.getInt(0));
		startManagingCursor(c2);
		if (c2.getCount() > 0) {
			do {
				dbAdapter.removeRecipeTag(c.getInt(0), c2.getInt(0));
				dbAdapter.deleteTagIfNoRecipe(c2.getInt(0));
			} while (c2.moveToNext());
		}
		dbAdapter.deleteRecipe(c.getInt(0));
		fillData();
		if (tagFilter == -1 && c.getCount() == 0) {
			TextView empty = (TextView) getListView().getEmptyView();
			empty.setText(R.string.recipelist_norecipe);
		}
		filterText(filterText.getText().toString());
	}

	@Override
	protected void onDestroy() {
		filterText.removeTextChangedListener(textWatcher);
		dbAdapter.close();
	    super.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("filterText", 
				filterText.getText().toString());
		outState.putSerializable("tagFilter", new Integer(tagFilter));
	}
}
