
const RecipeBuddy = {
    init() {
        this.setupDeletes();
    },



    setupDeletes() {

        document.querySelectorAll('form[action*="/delete"]').forEach(form => {
            form.onsubmit = () => confirm('Are you sure you want to delete this?');
        });


        document.querySelectorAll('button[onclick*="confirm"]').forEach(btn => {
            btn.onclick = (e) => {
                if (!confirm('Are you sure?')) e.preventDefault();
            };
        });
    }
};


document.addEventListener('DOMContentLoaded', () => RecipeBuddy.init());