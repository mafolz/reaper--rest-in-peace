class CreateArtists < ActiveRecord::Migration
  def self.up
    create_table :artists do |t|
      t.string  :name
      t.integer :genre_id
      t.integer :path_id
      t.timestamps
    end
    add_index :artists, :name 
  end

  def self.down
    drop_table :artists
  end
end
