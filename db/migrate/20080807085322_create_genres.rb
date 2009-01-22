require 'genre'
class CreateGenres < ActiveRecord::Migration
  def self.up
    create_table :genres do |t|
      t.string  :name
    end
    
    add_index :genres, :name, :unique => true 
  end

  def self.down
    drop_table :genres
  end
end
