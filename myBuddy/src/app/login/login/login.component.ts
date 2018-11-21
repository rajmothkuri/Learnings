import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'myBuddy-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  username: string;
  password: string;
  constructor() { }

  ngOnInit() {
  }

  login() {
    console.log(this.username);
    console.log(this.password);
  }

  register() {
    console.log('Inside Register');
  }
}
