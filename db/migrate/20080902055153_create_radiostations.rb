class CreateRadiostations < ActiveRecord::Migration
  def self.up
    create_table :radiostations do |t|
      t.text :address
      t.timestamps
    end
  end

  def self.down
    drop_table :radiostations
  end
end
