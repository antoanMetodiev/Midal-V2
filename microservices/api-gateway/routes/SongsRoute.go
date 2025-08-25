package routes

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

func SongsRoute(server *gin.Engine) {
	server.GET("/songs", getSongs)
}

func getSongs(c *gin.Context) {
	// Изпращане на GET заявка към localhost:8080/songs
	url := fmt.Sprintf("http://localhost:8080/songs/%d", 2)

	resp, err := http.Get(url)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	defer resp.Body.Close()
}
