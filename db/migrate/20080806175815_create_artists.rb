class CreateArtists < ActiveRecord::Migration
  def self.up
    create_table :artists do |t|
      t.text  :name
      t.integer :genre_id
      t.timestamps
    end
  end

  def self.down
    drop_table :artists
  end
end
