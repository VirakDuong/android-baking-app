package com.example.android.bakingapp.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * {@link Dao} which provides an api for all data operations with the {@link RecipeDatabase}
 */
@Dao
public interface RecipeDao {

    @Query("SELECT * FROM recipe")
    LiveData<List<RecipeEntry>> getAllRecipes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<RecipeEntry> recipeEntries);

    // ShoppingListEntry

    /**
     * Select all {@link ShoppingListEntry} entries
     *
     * @return {@link LiveData} with the list of shopping list entries
     */
    @Query("SELECT * FROM shopping_list")
    LiveData<List<ShoppingListEntry>> getAllShoppingList();

    /**
     * Insert a {@link ShoppingListEntry} into shopping_list table
     * @param shoppingListEntry The shopping list entry the user wants to add into the shopping list
     */
    @Insert
    void insertIngredient(ShoppingListEntry shoppingListEntry);

    @Delete
    void deleteIngredient(ShoppingListEntry shoppingListEntry);

    /**
     * Select ingredient_index where the value in column recipe_name is the given recipe name
     *
     * @param recipeName The recipe name
     * @return {@link LiveData} with the list of ingredient indices
     */
    @Query("SELECT ingredient_index FROM shopping_list WHERE recipe_name = :recipeName")
    LiveData<List<Integer>> getIndices(String recipeName);
}
