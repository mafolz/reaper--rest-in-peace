class CreateSongs < ActiveRecord::Migration
  def self.up
    create_table :songs do |t|
      t.text :title,:format,:location
      t.integer :artist_id
      t.boolean :localFlag, :default=>false
      t.date :created_at
    end
  end

  def self.down
    drop_table :songs
  end
end
