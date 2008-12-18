class RedirectorController < ApplicationController
  def goto
    redirect_to :controller=>params[:id]
  end
end
