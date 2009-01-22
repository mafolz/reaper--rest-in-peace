class CreateSongs < ActiveRecord::Migration
  def self.up
    create_table :songs do |t|
      t.string :title,:format
      t.text   :location
      t.integer :artist_id
      t.integer :album_id
      t.boolean :localFlag, :default=>false
      t.date :created_at
    end
    add_index :songs, :title
    add_index :songs, :artist_id
    add_index :songs, :album_id
  end

  def self.down
    drop_table :songs
  end
end
